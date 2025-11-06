package syspro.tm.jit;

import java.util.Arrays;
import java.util.Locale;

public enum OS {
    WINDOWS("Windows"), LINUX("Linux"), MACOS("macOS");

    public final String readableName;

    OS(String readableName) {
        this.readableName = readableName;
    }

    public static OS current() {
        final var name = System.getProperty("os.name").toLowerCase(Locale.ROOT);

        if (name.contains("win")) {
            return WINDOWS;
        }

        if (name.contains("linux")) {
            return LINUX;
        }

        if (name.contains("mac")) {
            return MACOS;
        }

        throw new RuntimeException("Only " + Arrays.toString(values()) + " OS are supported, OS is \"" + name + '"');
    }
}
