package syspro.tm.jit;

import java.lang.foreign.*;
import java.nio.file.Path;

public final class NativeLibraries {
    static final Linker linker = Linker.nativeLinker();

    private NativeLibraries() {
    }

    public static NativeLibrary load(Path libraryPath) {
        return new NativeLibrary(libraryPath);
    }
}
