package syspro.tm.test;

import syspro.tm.RegexEngine;
import syspro.tm.Utils;

import java.util.regex.Pattern;

public final class JavaRegexEngine implements RegexEngine {
    @Override
    public boolean matches(byte[] regex, byte[] input) {
        final var pattern = Pattern.compile(Utils.toJavaString(regex), Pattern.DOTALL);
        return pattern.matcher(Utils.toJavaString(input)).matches();
    }
}
