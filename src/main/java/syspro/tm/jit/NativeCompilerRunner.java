package syspro.tm.jit;

import org.apache.commons.io.IOUtils;
import syspro.tm.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class NativeCompilerRunner {
    private static volatile Optional<String> pickedMicrosoftCompiler;
    private static volatile Optional<String> pickedUnixCppCompiler;
    private static volatile Optional<String> pickedUnixCCompiler;
    private static volatile VisualStudioEnvironment visualStudioEnvironment;

    private NativeCompilerRunner() {
    }

    public static Path compile(String code, LanguageVersion languageVersion) {
        final var suffix = switch (languageVersion.language) {
            case Cpp -> ".cpp";
            case C -> ".c";
        };
        try {
            final var sourceFile = Files.createTempFile("regex", suffix);
            sourceFile.toFile().deleteOnExit();
            Files.writeString(sourceFile, code);

            if (OS.current() == OS.WINDOWS) {
                return compileMicrosoft(languageVersion, sourceFile, suffix);
            }

            return compileUnix(languageVersion, sourceFile, suffix);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path compileMicrosoft(LanguageVersion languageVersion, Path sourceFile, String suffix) throws IOException, InterruptedException {
        final var visualStudioEnvironment = visualStudioEnvironment();
        final var pickedCompiler = visualStudioEnvironment.clExe;
        final var args = new ArrayList<String>();
        args.add("/nologo");
        args.add("/std:" + formatMicrosoftLanguageVersion(languageVersion));
        args.add("/GL");
        args.add("/O2");
        args.add("/EHsc");
        args.add("/DNDEBUG");
        args.add("/LD");
        args.add('"' + sourceFile.toAbsolutePath().toString() + '"');
        for (final var provider : Configuration.configurationProviders()) {
            provider.adjustMicrosoftCompilerOptions(pickedCompiler, args);
        }
        args.addFirst(pickedCompiler);
        final var clOutput = runProcess(visualStudioEnvironment.environment, args.toArray(new String[0]));
        final var dllFile = sourceFile.resolveSibling(sourceFile.getFileName().toString().replace(suffix, ".dll"));
        if (dllFile.toFile().exists()) {
            return dllFile;
        }
        System.err.println(clOutput);
        return null;
    }

    private static Path compileUnix(LanguageVersion languageVersion, Path sourceFile, String suffix) throws IOException, InterruptedException {
        final var pickedCompiler = pickedUnixCompiler(languageVersion.language);
        final var libExtension = switch (OS.current()) {
            case WINDOWS -> ".dll";
            case LINUX -> ".so";
            case MACOS -> ".dylib";
        };
        final var libFile = sourceFile.resolveSibling(sourceFile.getFileName().toString().replace(suffix, libExtension));
        final var args = new ArrayList<String>();
        args.add("-shared");
        args.add("-std=" + formatUnixLanguageVersion(languageVersion));
        args.add("-O2");
        args.add("-o");
        args.add(libFile.toAbsolutePath().toString());
        args.add("-fPIC");
        args.add(sourceFile.toAbsolutePath().toString());
        for (final var provider : Configuration.configurationProviders()) {
            provider.adjustUnixCompilerOptions(pickedCompiler, args);
        }
        args.addFirst(pickedCompiler);
        final var clOutput = runProcess(args.toArray(new String[0]));
        if (libFile.toFile().exists()) {
            return libFile;
        }
        System.err.println(clOutput);
        return null;
    }

    @SuppressWarnings("OptionalAssignedToNull")
    private static String pickedMicrosoftCompiler() throws IOException, InterruptedException {
        final var cachedValue = pickedMicrosoftCompiler;
        String pickedCompiler;
        if (cachedValue != null) {
            pickedCompiler = cachedValue.orElse(null);
        } else {
            pickedCompiler = pickMicrosoftCompiler();
            pickedMicrosoftCompiler = Optional.ofNullable(pickedCompiler);
        }
        if (pickedCompiler == null) {
            throw new UnsupportedOperationException("Could not find native compiler (try overriding syspro.tm.ConfigurationProvider.pickMicrosoftCompiler)");
        }
        return pickedCompiler;
    }

    private static VisualStudioEnvironment visualStudioEnvironment() throws IOException, InterruptedException {
        final var cachedValue = visualStudioEnvironment;
        VisualStudioEnvironment visualStudioEnvironment;
        if (cachedValue != null) {
            visualStudioEnvironment = cachedValue;
        } else {
            visualStudioEnvironment = computeVisualStudioEnvironment();
            NativeCompilerRunner.visualStudioEnvironment = visualStudioEnvironment;
        }
        if (visualStudioEnvironment == null) {
            throw new UnsupportedOperationException("Could not find native compiler (try overriding syspro.tm.ConfigurationProvider.pickMicrosoftCompiler)");
        }
        return visualStudioEnvironment;
    }

    @SuppressWarnings("OptionalAssignedToNull")
    private static String pickedUnixCompiler(Language language) {
        final var cachedValue = language == Language.Cpp ? pickedUnixCppCompiler : pickedUnixCCompiler;
        String pickedCompiler;
        if (cachedValue != null) {
            pickedCompiler = cachedValue.orElse(null);
        } else {
            pickedCompiler = pickUnixCompiler(language);
            final var pickedCompilerOptional = Optional.ofNullable(pickedCompiler);
            if (language == Language.Cpp) {
                pickedUnixCppCompiler = pickedCompilerOptional;
            } else {
                pickedUnixCCompiler = pickedCompilerOptional;
            }
        }
        if (pickedCompiler == null) {
            throw new UnsupportedOperationException("Could not find native compiler (try overriding syspro.tm.ConfigurationProvider.pickUnixCompiler)");
        }
        return pickedCompiler;
    }

    private static String pickMicrosoftCompiler() throws IOException, InterruptedException {
        final var hostName = switch (Architecture.current()) {
            case AMD64 -> "vcvars64";
            case AARCH64 -> "vcvarsamd64_arm64";
        };
        final var vswhereOutput = runProcess("C:\\Program Files (x86)\\Microsoft Visual Studio\\Installer\\vswhere.exe", "-sort", "-prerelease", "-property", "installationPath");
        final var vcBatchFiles = new ArrayList<String>();
        for (final var line : vswhereOutput.lines().toList()) {
            final var batPath = Paths.get(line).resolve("VC", "Auxiliary", "Build", hostName + ".bat");
            final var vcBatchFile = batPath.toFile();
            if (vcBatchFile.exists()) {
                vcBatchFiles.add(vcBatchFile.getAbsolutePath());
            }
        }
        String pickedCompiler = null;
        for (final var provider : Configuration.configurationProviders()) {
            final var pick = provider.pickMicrosoftCompiler(vcBatchFiles);
            if (pick != null) {
                pickedCompiler = pick;
            }
        }
        if (pickedCompiler == null && !vcBatchFiles.isEmpty()) {
            pickedCompiler = vcBatchFiles.getFirst();
        }
        return pickedCompiler;
    }

    private static String pickUnixCompiler(Language language) {
        final var prefixes = new String[]{language == Language.Cpp ? "g++" : "gcc", language == Language.Cpp ? "clang++" : "clang"};
        final var compilers = findExecutablesOnPath(name -> {
            for (final var prefix : prefixes) {
                if (name.startsWith(prefix)) {
                    return true;
                }
            }
            return false;
        });
        String pickedCompiler = null;
        for (final var provider : Configuration.configurationProviders()) {
            final var pick = provider.pickUnixCompiler(compilers);
            if (pick != null) {
                pickedCompiler = pick;
            }
        }
        if (pickedCompiler == null && !compilers.isEmpty()) {
            pickedCompiler = compilers.getFirst();
        }
        return pickedCompiler;
    }

    private static List<String> findExecutablesOnPath(Predicate<String> nameFilter) {
        final var results = new ArrayList<String>();
        for (final var pathItem : System.getenv("PATH").split(File.pathSeparator)) {
            final var directory = new File(pathItem);
            if (!directory.exists() || !directory.isDirectory()) {
                continue;
            }

            for (final var file : Objects.requireNonNull(directory.listFiles())) {
                if (file.isFile() && file.canExecute() && nameFilter.test(file.getName())) {
                    results.add(file.getAbsolutePath());
                }
            }
        }
        return results;
    }

    private static String formatMicrosoftLanguageVersion(LanguageVersion languageVersion) {
        String languageRevision;
        if (languageVersion.language == Language.Cpp && languageVersion.revision == 14) {
            languageRevision = "c++14";
        } else if (languageVersion.language == Language.Cpp && languageVersion.revision == 17) {
            languageRevision = "c++17";
        } else if (languageVersion.language == Language.Cpp && languageVersion.revision == 20) {
            languageRevision = "c++20";
        } else if (languageVersion.language == Language.C && languageVersion.revision == 11) {
            languageRevision = "c11";
        } else if (languageVersion.language == Language.C && languageVersion.revision == 17) {
            languageRevision = "c17";
        } else {
            throw new IllegalArgumentException("Unsupported language and version: " + languageVersion);
        }
        return languageRevision;
    }

    private static String formatUnixLanguageVersion(LanguageVersion languageVersion) {
        final var part1 = languageVersion.extensions ? "gnu" : "c";
        final var part2 = languageVersion.language == Language.Cpp ? "++" : "";
        final var part3 = languageVersion.revision == 3 ? "03" : Integer.toString(languageVersion.revision);
        return part1 + part2 + part3;
    }

    private static String runProcess(String... command) throws IOException, InterruptedException {
        return runProcess(null, command);
    }

    private static String runProcess(Map<String, String> environment, String... command) throws IOException, InterruptedException {
        final var builder = new ProcessBuilder(command).directory(new File(System.getProperty("java.io.tmpdir")));
        if (environment != null && !environment.isEmpty()) {
            builder.environment().putAll(environment);
        }
        final var process = builder.redirectErrorStream(true).start();
        final var output = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());
        process.waitFor();
        return output;
    }

    private static VisualStudioEnvironment computeVisualStudioEnvironment() throws IOException, InterruptedException {
        final var pickedCompiler = pickedMicrosoftCompiler();
        final var batFile = Files.createTempFile("regex", ".bat");
        final var beforeFile = Files.createTempFile("regex-vs-before", ".txt");
        final var afterFile = Files.createTempFile("regex-vs-after", ".txt");
        final var clExeFile = Files.createTempFile("regex-vs-cl", ".txt");
        batFile.toFile().deleteOnExit();
        beforeFile.toFile().deleteOnExit();
        afterFile.toFile().deleteOnExit();
        clExeFile.toFile().deleteOnExit();
        final var batLines = new ArrayList<String>();
        batLines.add("set > \"" + beforeFile.toAbsolutePath() + "\"");
        batLines.add("call \"" + pickedCompiler + "\"");
        batLines.add("set > \"" + afterFile.toAbsolutePath() + "\"");
        batLines.add("where cl.exe > \"" + clExeFile.toAbsolutePath() + "\"");
        Files.writeString(batFile, String.join(System.lineSeparator(), batLines));
        final var clOutput = runProcess("cmd.exe", "/C", batFile.toAbsolutePath().toString());
        if (beforeFile.toFile().length() > 0 && afterFile.toFile().length() > 0 && clExeFile.toFile().length() > 0) {
            final var before = Files.readAllLines(beforeFile);
            final var after = Files.readAllLines(afterFile);
            final var clExeText = Files.readString(clExeFile);
            batFile.toFile().delete();
            beforeFile.toFile().delete();
            afterFile.toFile().delete();
            clExeFile.toFile().delete();
            return computeVisualStudioEnvironment(before, after, clExeText.trim());
        }
        System.err.println(clOutput);
        return null;
    }

    private static VisualStudioEnvironment computeVisualStudioEnvironment(List<String> beforeList, List<String> afterList, String clExeText) {
        final var before = envFileToMap(beforeList);
        final var after = envFileToMap(afterList);
        final var difference = new HashMap<String, String>();
        final var newKeys = new HashSet<>(after.keySet());
        newKeys.removeAll(before.keySet());
        final var commonKeys = new HashSet<>(after.keySet());
        commonKeys.retainAll(before.keySet());
        for (final var key : newKeys) {
            final var newValue = after.get(key);
            difference.put(key, newValue);
        }
        for (final var key : commonKeys) {
            final var oldValue = before.get(key);
            final var newValue = after.get(key);
            if (oldValue.equals(newValue)) {
                continue;
            }

            difference.put(key, newValue);
        }

        if (new File(clExeText).canExecute()) {
            return new VisualStudioEnvironment(clExeText, difference);
        }
        return null;
    }

    private static Map<String, String> envFileToMap(List<String> lines) {
        final var result = new HashMap<String, String>();
        for (var line : lines) {
            final var eq = line.indexOf('=');
            if (eq == -1) {
                continue;
            }
            result.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
        }
        return result;
    }

    private static final class VisualStudioEnvironment {
        public final String clExe;
        public final Map<String, String> environment;

        private VisualStudioEnvironment(String clExe, Map<String, String> environment) {
            this.clExe = clExe;
            this.environment = environment;
        }
    }
}
