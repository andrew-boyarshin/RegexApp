package syspro.tm.test;

import syspro.tm.RegexEngine;
import syspro.tm.Utils;

import java.util.HashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class JavaCachingRegexEngine implements RegexEngine, Function<byte[], Pattern> {
    private static final HashMap<byte[], Pattern> regexCache = new HashMap<>();

    @Override
    public boolean matches(byte[] regex, byte[] input) {
        final var pattern = regexCache.computeIfAbsent(regex, this);
        return pattern.matcher(Utils.toJavaString(input)).matches();
    }

    @Override
    public Pattern apply(byte[] regex) {
        return Pattern.compile(Utils.toJavaString(regex), Pattern.DOTALL);
    }
}
