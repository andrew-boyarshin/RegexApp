package syspro.tm.jit;

import java.util.Arrays;
import java.util.Locale;

public enum Architecture {
    AMD64("AMD64"), AARCH64("AArch64");

    public final String readableName;

    Architecture(String readableName) {
        this.readableName = readableName;
    }

    public static Architecture current() {
        final var arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (arch.contains("aarch64")) {
            return AARCH64;
        }

        if ((arch.contains("86") || arch.contains("amd")) && arch.contains("64")) {
            return AMD64;
        }

        throw new RuntimeException("Only " + Arrays.toString(values()) + " architectures are supported, architecture is \"" + arch + '"');
    }
}
