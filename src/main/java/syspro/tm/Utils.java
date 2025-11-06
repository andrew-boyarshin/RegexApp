package syspro.tm;

import java.util.Locale;

public final class Utils {
    private Utils() {
    }

    public static boolean isMetaCharacter(byte c) {
        return c == '\\' || c == '*' || c == '+' || c == '?' || c == '|' || c == '{' || c == '}' || c == '[' || c == ']' || c == '(' || c == ')' || c == '.' || c == '-' || c == '^';
    }

    public static String toLiteralPatternCharacter(byte value, char quote) {
        if (value == 0) {
            // C++ regex: <NUL>
            // C++ string literal: \0
            // Java string literal: \\0
            return "\\0";
        }

        if (value == '\n') {
            return "\\n";
        }

        if (value == '\r') {
            return "\\r";
        }

        if (value == '\t') {
            return "\\t";
        }

        if (value == '\f') {
            return "\\f";
        }

        if (value == '\\') {
            // C++ regex: \\
            // C++ string literal: \\\\
            // Java string literal: \\\\\\\\
            return "\\\\\\\\";
        }

        if (value == quote) {
            // C++ regex: "
            // C++ string literal: \"
            // Java string literal: \\"
            return "\\" + ((char) value);
        }

        if (isMetaCharacter(value)) {
            // C++ regex: \*
            // C++ string literal: \\*
            // Java string literal: \\\\*
            return "\\\\" + ((char) value);
        }

        if (value >= 32 && value <= 126) {
            return Character.toString((char) value);
        }

        var hexString = Integer.toHexString(Byte.toUnsignedInt(value));
        if (hexString.length() < 2) {
            hexString = "0" + hexString;
        }
        // C++ regex: \xff
        // C++ string literal: \\xff
        // Java string literal: \\\\xff
        return "\\\\x" + hexString;
    }

    public static String toHumanString(byte value) {
        if (value == 0) {
            return "\\0";
        }

        if (value == '\n') {
            return "\\n";
        }

        if (value == '\r') {
            return "\\r";
        }

        if (value == '\t') {
            return "\\t";
        }

        if (value == '\f') {
            return "\\f";
        }

        if (value >= 32 && value <= 126) {
            return Character.toString((char) value);
        }

        var hexString = Integer.toHexString(Byte.toUnsignedInt(value));
        if (hexString.length() < 2) {
            hexString = "0" + hexString;
        }
        return "\\x" + hexString.toUpperCase(Locale.ROOT);
    }

    public static String toRegexString(byte value) {
        if (value == 0) {
            return "\\0";
        }

        if (value == '\n') {
            return "\\n";
        }

        if (value == '\r') {
            return "\\r";
        }

        if (value == '\t') {
            return "\\t";
        }

        if (value == '\f') {
            return "\\f";
        }

        if (isMetaCharacter(value)) {
            return "\\" + ((char) value);
        }

        if (value >= 33 && value <= 126) {
            return Character.toString((char) value);
        }

        var hexString = Integer.toHexString(Byte.toUnsignedInt(value));
        if (hexString.length() < 2) {
            hexString = "0" + hexString;
        }
        return "\\x" + hexString;
    }

    public static byte[] toBytes(String text) {
        final var chars = text.toCharArray();
        final var charsLength = chars.length;
        final var bytes = new byte[charsLength];
        for (var i = 0; i < charsLength; i++) {
            final var c = chars[i];
            if (c >= 256) {
                throw new IllegalArgumentException('"' + text + "\" has characters beyond 256: " + Integer.toHexString(c));
            }
            bytes[i] = (byte) c;
        }
        return bytes;
    }

    public static String toJavaString(byte[] bytes) {
        final var bytesLength = bytes.length;
        final var chars = new char[bytesLength];
        for (var i = 0; i < bytesLength; i++) {
            chars[i] = (char) Byte.toUnsignedInt(bytes[i]);
        }
        return new String(chars);
    }
}
