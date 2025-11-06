package syspro.tm.jit;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.HashMap;

public final class NativeLibrary {
    private static final FunctionDescriptor MATCHES_DESCRIPTOR = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, sizeIntegralLayout());
    private final Path libraryFile;
    private final Arena arena;
    private final SymbolLookup lookup;
    private final HashMap<String, MethodHandle> functions = new HashMap<>();
    private MethodHandle matchesHandle;

    NativeLibrary(Path libraryFile) {
        this.libraryFile = libraryFile;
        arena = Arena.ofAuto();
        lookup = SymbolLookup.libraryLookup(libraryFile, arena);
    }

    private static MemoryLayout sizeIntegralLayout() {
        final var pointerSize = ValueLayout.ADDRESS.byteSize();
        return switch ((int) pointerSize) {
            case 8 -> ValueLayout.JAVA_LONG;
            case 4 -> ValueLayout.JAVA_INT;
            default ->
                    throw new UnsupportedOperationException(pointerSize + " byte wide pointer architecture is not supported.");
        };
    }

    public String libraryName() {
        return libraryFile.toFile().getName();
    }

    public MemorySegment findSymbol(String name) {
        return lookup.find(name).orElseThrow(() -> new RuntimeException('`' + name + "` was not found in `" + libraryName() + "`."));
    }

    public MethodHandle findFunction(String name, FunctionDescriptor function) {
        return functions.computeIfAbsent(name, _ -> NativeLibraries.linker.downcallHandle(findSymbol(name), function));
    }

    public void setMatchesFunction(String name) {
        final var handle = matchesHandle;
        if (handle != null) {
            throw new IllegalStateException("Match function is already set for `" + libraryName() + "` library.");
        }

        matchesHandle = findFunction(name, MATCHES_DESCRIPTOR);
    }

    public boolean callMatches(byte[] input) {
        try (final var arena = Arena.ofConfined()) {
            final var inputBuffer = arena.allocateFrom(ValueLayout.JAVA_BYTE, input);
            final var pointerSize = ValueLayout.ADDRESS.byteSize();
            final int result = switch ((int) pointerSize) {
                case 8 -> (int) matchesHandle.invokeExact(inputBuffer, (long) input.length);
                case 4 -> (int) matchesHandle.invokeExact(inputBuffer, input.length);
                default ->
                        throw new UnsupportedOperationException(pointerSize + " byte wide pointer architecture is not supported.");
            };
            return result != 0;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
