package syspro.tm;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public final class DefaultTestCases implements ConfigurationProvider {
    @Override
    public void provideTestData(TestDataListBuilder builder) {
        builder.add("[a]|[b-b]", "", false);
        builder.add("[a]|[b-b]", "a", true);
        builder.add("[a]|[b-b]", "aa", false);
        builder.add("[a]|[b-b]", "b", true);
        builder.add("[a]|[b-b]", "ab", false);
        builder.add("[a]|[b-b]", "ba", false);
        builder.add("[a]|[b-b]", " aa", false);
        builder.add("[a]|[b-b]", "aa aa", false);
        builder.add("([a]|[b-b])*", "", true);
        builder.add("([a]|[b-b])*", "a", true);
        builder.add("([a]|[b-b])*", "aa", true);
        builder.add("([a]|[b-b])*", "b", true);
        builder.add("([a]|[b-b])*", "ab", true);
        builder.add("([a]|[b-b])*", "ba", true);
        builder.add("([a]|[b-b])*", " aa", false);
        builder.add("([a]|[b-b])*", "aa aa", false);
        builder.add("([a]|[b-b])+", "", false);
        builder.add("([a]|[b-b])+", "a", true);
        builder.add("([a]|[b-b])+", "aa", true);
        builder.add("([a]|[b-b])+", "b", true);
        builder.add("([a]|[b-b])+", "ab", true);
        builder.add("([a]|[b-b])+", "ba", true);
        builder.add("([a]|[b-b])+", " aa", false);
        builder.add("([a]|[b-b])+", "aa aa", false);
        builder.add("((..)|(.))", "", false);
        builder.add("((..)|(.))((..)|(.))", "", false);
        builder.add("((..)|(.))+", "", false);
        builder.add("((..)|(.)){3}", "", false);
        builder.add("((..)|(.))*", "", true);
        builder.add("((..)|(.))", "a", true);
        builder.add("((..)|(.))((..)|(.))", "a", false);
        builder.add("((..)|(.))+", "a", true);
        builder.add("((..)|(.)){3}", "a", false);
        builder.add("((..)|(.))*", "a", true);
        builder.add("a(b?)?", "ab", true);
        builder.add("(a*)*", "", true);
        builder.add("(a*)*", "a", true);
        builder.add("(a*)*", "x", false);
        builder.add("(a+)*", "", true);
        builder.add("(a+)*", "a", true);
        builder.add("(a+)*", "x", false);
        builder.add("(a*)+", "", true);
        builder.add("(a*)+", "a", true);
        builder.add("(a*)+", "x", false);
        builder.add("(a+)+", "", false);
        builder.add("(a+)+", "a", true);
        builder.add("(a+)+", "x", false);
        builder.add("", "", true);
        builder.add("", "a", false);
        builder.add("|", "", true);
        builder.add("|", "a", false);
        builder.add("a* ?", "", true);
        builder.add("a* ?", "aaa", true);
        builder.add("a* ?", " ", true);
        builder.add("a* ?", "  ", false);
        builder.add("a* ?", "aaa ", true);
        builder.add("a* ?", "a a", false);
        builder.add("\01.?[\300-\377]+\02", "\01\03\310\320\02", true);
        builder.add("[.]", "a", false);
        builder.add("[.]", ".", true);
        builder.add("[.]", "", false);
        builder.add("[^.]", "a", true);
        builder.add("[^.]", ".", false);
        builder.add("[^.]", "", false);
        builder.add("\\**", "", true);
        builder.add("\\**", "*", true);
        builder.add("\\**", "**", true);
        builder.add("\\++", "", false);
        builder.add("\\++", "+", true);
        builder.add("\\++", "++", true);
        builder.add("\\?+", "", false);
        builder.add("\\?+", "?", true);
        builder.add("\\?+", "??", true);
        builder.add("(\\??)?", "", true);
        builder.add("(\\??)?", "?", true);
        builder.add("(\\??)?", "??", false);
        builder.add("(\\?+)?", "", true);
        builder.add("(\\?+)?", "?", true);
        builder.add("(\\?+)?", "??", true);
        builder.add("(\\?*)?", "", true);
        builder.add("(\\?*)?", "?", true);
        builder.add("(\\?*)?", "??", true);
        builder.add("((a*|b*))*", "aaabbbaaa", true);
        builder.add("[^a-z]", "\0", true);
        builder.add("[^a-z]", "0", true);
        builder.add("[^a-z]", "\n", true);
        builder.add("[^a-z]", "f", false);
        builder.add("[^a-z]", "a", false);
        builder.add("[^a-z]", "z", false);
        builder.add("\\\".*\\\"\\s*(;.*)?", "\"1234\"", true);
        builder.add("\\\".*\\\"\\s*(;.*)?", "\"abcd\" ;", true);
        builder.add("\\\".*\\\"\\s*(;.*)?", "\"\" ; rhubarb", true);
        builder.add("\\\".*\\\"\\s*(;.*)?", "\"1234\" : things", false);
        builder.add("[aeiou\\d]{4,5}", "uoie", true);
        builder.add("[aeiou\\d]{4,5}", "1234", true);
        builder.add("[aeiou\\d]{4,5}", "12345", true);
        builder.add("[aeiou\\d]{4,5}", "aaaaa", true);
        builder.add("[aeiou\\d]{4,5}", "123456", false);
        builder.add("([^a]*)*", "b", true);
        builder.add("([^a]*)*", "bbbb", true);
        builder.add("([^a]*)*", "aaa", false);
        builder.add("([^ab]*)*", "cccc", true);
        builder.add("([^ab]*)*", "abab", false);
        builder.add("(([a]*)?)*", "a", true);
        builder.add("(([a]*)?)*", "aaaa", true);
        builder.add("(([ab]*)?)*", "a", true);
        builder.add("(([ab]*)?)*", "b", true);
        builder.add("(([ab]*)?)*", "abab", true);
        builder.add("(([ab]*)?)*", "baba", true);
        builder.add("(([^a]*)?)*", "b", true);
        builder.add("(([^a]*)?)*", "bbbb", true);
        builder.add("(([^a]*)?)*", "aaa", false);
        builder.add("(([^ab]*)?)*", "c", true);
        builder.add("(([^ab]*)?)*", "cccc", true);
        builder.add("(([^ab]*)?)*", "baba", false);
        builder.add("([abc])*bcd", "abcd", true);
        builder.add("([abc])*bcd", "abbcd", true);
        builder.add("((((((((((((((((((((x))))))))))))))))))))", "x", true);
        builder.add("((((((((((((((((((((x))))))))))))))))))))", "", false);
        builder.add("\\w*I\\w*", "", false);
        builder.add("\\w*I\\w*", "I", true);
        builder.add("\\w*I\\w*", "Inc", true);
        builder.add("\\w*I\\w*", "Inc.", false);
        builder.add(".+\nabc", "a\nabc", true);
        builder.add("a(.*)?[b\n]", "a12345b", true);
        builder.add("a(.*)?[b\n]", "a12345\n", true);
        builder.add("((.*)?)(\n|\r\n?)", "ab\r", true);
        builder.add("((.*)?)(\n|\r\n?)", "ab\\r", false);
        builder.add("((.*)?)(\n|\r\n?)", "ab\\n", false);
        builder.add("[\r\n]A", "\r\nA", false);
        builder.add("[\r\n]A", "\rA", true);
        builder.add("[\r\n]A", "\nA", true);
        builder.add("[\r\n]A", "A", false);
        builder.add("(\r|\n)A", "\r\nA", false);
        builder.add("(\r|\n)A", "\rA", true);
        builder.add("(\r|\n)A", "\nA", true);
        builder.add("(\r|\n)A", "A", false);
        builder.add("a.c", "a\0c", true);
        builder.add("a.c", "a\0d", false);
        builder.add("a\0c", "a\0c", true);
        builder.add("a\0c", "a\0d", false);

        try (var _ = builder.benchmarkGroup()) {
            builder.add("(a?){20}a{20}", "aaaaaaaaaaaaaaaaaaaa", true);
            builder.add("(a+)+", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
            builder.add("(a+)+", "aaaaaaaaaaaaaaaaaaaaaaaaaaa!", false);
            builder.add("(([0-9a-fA-F]{1,4}:)*([0-9a-fA-F]{1,4}))*(::)", "b51:4:1DB:9EE1:5:27d60:f44:D4:cd:E:5:0A5:4a:D24:41Ad:", false);
            builder.add("[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])?@.*", "test@contoso.com", true);
            builder.add("(([A-Z]\\w*)+\\.)*[A-Z]\\w*", "aaaaaaaaaaaaaaaaaaaaaa.", false);
            builder.add(".*(es).*", "Essential services are provided by regular expressions.", true);
        }

        final var sherlock = loadSherlockBytes();
        builder.addSlidingWindowBenchmark(Utils.toBytes(".*Sherlock Holmes.*"), sherlock, 40);
        builder.addSlidingWindowBenchmark(Utils.toBytes(".*Sherlock\\s+Holmes.*"), sherlock, 40);
        builder.addSlidingWindowBenchmark(Utils.toBytes(".*(Holmes.{0,25}Watson|Watson.{0,25}Holmes).*"), sherlock, 40);
        builder.addSlidingWindowBenchmark(Utils.toBytes(".*[a-zA-Z]+ing.*"), sherlock, 40);
        builder.addSlidingWindowBenchmark(Utils.toBytes(".*\\s[a-zA-Z]{0,12}ing\\s.*"), sherlock, 40);
    }

    public static byte[] loadSherlockBytes() {
        try {
            return IOUtils.resourceToByteArray("sherlock.txt", DefaultTestCases.class.getClassLoader());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
