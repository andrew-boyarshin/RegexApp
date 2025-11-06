package syspro.tm;

import java.util.List;

public interface ConfigurationProvider {
    default void provideTestData(TestDataListBuilder builder) {
    }

    default boolean shouldSkipEngine(RegexEngine engine) {
        return false;
    }

    default boolean shouldSkipTestCase(ConfigurationProvider provider, byte[] regex, byte[] input, boolean benchmark) {
        return false;
    }

    default boolean shouldDisableProgressUpdates() {
        return false;
    }

    /**
     * Examples:
     * <ul>
     *     <li>{@literal C:\Program Files\Microsoft Visual Studio\18\Insiders\VC\Auxiliary\Build\vcvars64.bat}</li>
     *     <li>{@literal C:\Program Files\Microsoft Visual Studio\2022\Preview\VC\Auxiliary\Build\vcvars64.bat}</li>
     * </ul>
     */
    default String pickMicrosoftCompiler(List<String> compilerPaths) {
        return null;
    }

    default void adjustMicrosoftCompilerOptions(String compilerPath, List<String> compilerOptions) {
    }

    /**
     * Examples:
     * <ul>
     *     <li>{@literal /bin/clang++-20}</li>
     *     <li>{@literal /bin/g++}</li>
     * </ul>
     */
    default String pickUnixCompiler(List<String> compilerPaths) {
        return null;
    }

    default void adjustUnixCompilerOptions(String compilerPath, List<String> compilerOptions) {
    }
}
