package syspro.tm.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import syspro.tm.Configuration;
import syspro.tm.TestData;
import syspro.tm.Utils;
import syspro.tm.regex.Regex;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class EngineTest {
    private final JavaCachingRegexEngine myRegexEngine = new JavaCachingRegexEngine();

    public static Stream<TestData> singleConfiguredTest() {
        return Configuration.testData().stream().filter(data -> data.slidingWindowSize == null);
    }

    public static Stream<byte[]> singleConfiguredRegex() {
        return Configuration.testData().stream().map(data -> data.regex);
    }

    private boolean matches(String regex, String input) {
        return myRegexEngine.matches(Utils.toBytes(regex), Utils.toBytes(input));
    }

    @Test
    public void basicTest() {
        assertTrue(matches("a[bB][0-9]\\d\\w?.\\s", "aB42_\n\f"));
        assertFalse(matches("a[bB][0-9]\\d\\w?", "aB42-"));
    }

    @ParameterizedTest
    @MethodSource
    public void singleConfiguredTest(TestData data) {
        assertEquals(data.expected, myRegexEngine.matches(data.regex, data.input));
    }

    @ParameterizedTest
    @MethodSource
    public void singleConfiguredRegex(byte[] data) {
        assertDoesNotThrow(() -> {
            Regex.parse(data);
        });
    }
}
