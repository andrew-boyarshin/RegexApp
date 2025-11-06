package syspro.tm.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import syspro.tm.Utils;
import syspro.tm.regex.RegexCharacter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class UtilsTest {
    @Test
    public void toLiteralPatternCharacterTest() {
        final var expected = new String[]{
                "\\0", "\\\\x01", "\\\\x02", "\\\\x03", "\\\\x04", "\\\\x05", "\\\\x06", "\\\\x07", "\\\\x08",
                "\\t", "\\n", "\\\\x0b", "\\f", "\\r", "\\\\x0e", "\\\\x0f", "\\\\x10", "\\\\x11", "\\\\x12",
                "\\\\x13", "\\\\x14", "\\\\x15", "\\\\x16", "\\\\x17", "\\\\x18", "\\\\x19",
                "\\\\x1a", "\\\\x1b", "\\\\x1c", "\\\\x1d", "\\\\x1e", "\\\\x1f",
                " ", "!", "\\\"", "#", "$", "%", "&", "'",
                "\\\\(", "\\\\)", "\\\\*", "\\\\+", ",", "\\\\-", "\\\\.", "/",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "\\\\?", "@",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z",
                "\\\\[", "\\\\\\\\", "\\\\]", "\\\\^", "_", "`",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                "s", "t", "u", "v", "w", "x", "y", "z",
                "\\\\{", "\\\\|", "\\\\}", "~", "\\\\x7f",
                "\\\\x80", "\\\\x81", "\\\\x82", "\\\\x83", "\\\\x84", "\\\\x85", "\\\\x86", "\\\\x87", "\\\\x88",
                "\\\\x89", "\\\\x8a", "\\\\x8b", "\\\\x8c", "\\\\x8d", "\\\\x8e", "\\\\x8f",
                "\\\\x90", "\\\\x91", "\\\\x92", "\\\\x93", "\\\\x94", "\\\\x95", "\\\\x96", "\\\\x97", "\\\\x98",
                "\\\\x99", "\\\\x9a", "\\\\x9b", "\\\\x9c", "\\\\x9d", "\\\\x9e", "\\\\x9f",
                "\\\\xa0", "\\\\xa1", "\\\\xa2", "\\\\xa3", "\\\\xa4", "\\\\xa5", "\\\\xa6", "\\\\xa7", "\\\\xa8",
                "\\\\xa9", "\\\\xaa", "\\\\xab", "\\\\xac", "\\\\xad", "\\\\xae", "\\\\xaf",
                "\\\\xb0", "\\\\xb1", "\\\\xb2", "\\\\xb3", "\\\\xb4", "\\\\xb5", "\\\\xb6", "\\\\xb7", "\\\\xb8",
                "\\\\xb9", "\\\\xba", "\\\\xbb", "\\\\xbc", "\\\\xbd", "\\\\xbe", "\\\\xbf",
                "\\\\xc0", "\\\\xc1", "\\\\xc2", "\\\\xc3", "\\\\xc4", "\\\\xc5", "\\\\xc6", "\\\\xc7", "\\\\xc8",
                "\\\\xc9", "\\\\xca", "\\\\xcb", "\\\\xcc", "\\\\xcd", "\\\\xce", "\\\\xcf",
                "\\\\xd0", "\\\\xd1", "\\\\xd2", "\\\\xd3", "\\\\xd4", "\\\\xd5", "\\\\xd6", "\\\\xd7", "\\\\xd8",
                "\\\\xd9", "\\\\xda", "\\\\xdb", "\\\\xdc", "\\\\xdd", "\\\\xde", "\\\\xdf",
                "\\\\xe0", "\\\\xe1", "\\\\xe2", "\\\\xe3", "\\\\xe4", "\\\\xe5", "\\\\xe6", "\\\\xe7", "\\\\xe8",
                "\\\\xe9", "\\\\xea", "\\\\xeb", "\\\\xec", "\\\\xed", "\\\\xee", "\\\\xef",
                "\\\\xf0", "\\\\xf1", "\\\\xf2", "\\\\xf3", "\\\\xf4", "\\\\xf5", "\\\\xf6", "\\\\xf7", "\\\\xf8",
                "\\\\xf9", "\\\\xfa", "\\\\xfb", "\\\\xfc", "\\\\xfd", "\\\\xfe", "\\\\xff"
        };
        final var actual = new String[256];
        for (short i = 0; i < actual.length; i++) {
            actual[i] = Utils.toLiteralPatternCharacter((byte) i, '"');
        }
        assertArrayEquals(expected, actual);
    }

    @Test
    public void toRegexStringTest() {
        final var expected = new String[]{
                "\\0", "\\x01", "\\x02", "\\x03", "\\x04", "\\x05", "\\x06", "\\x07", "\\x08",
                "\\t", "\\n", "\\x0b", "\\f", "\\r", "\\x0e", "\\x0f", "\\x10", "\\x11", "\\x12", "\\x13", "\\x14",
                "\\x15", "\\x16", "\\x17", "\\x18", "\\x19", "\\x1a", "\\x1b", "\\x1c", "\\x1d", "\\x1e", "\\x1f",
                "\\x20", "!", "\"", "#", "$", "%", "&", "'", "\\(", "\\)", "\\*", "\\+", ",", "\\-", "\\.", "/",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "\\?", "@",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z",
                "\\[", "\\\\", "\\]", "\\^", "_", "`",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                "s", "t", "u", "v", "w", "x", "y", "z",
                "\\{", "\\|", "\\}", "~", "\\x7f",
                "\\x80", "\\x81", "\\x82", "\\x83", "\\x84", "\\x85", "\\x86", "\\x87", "\\x88", "\\x89",
                "\\x8a", "\\x8b", "\\x8c", "\\x8d", "\\x8e", "\\x8f",
                "\\x90", "\\x91", "\\x92", "\\x93", "\\x94", "\\x95", "\\x96", "\\x97", "\\x98", "\\x99",
                "\\x9a", "\\x9b", "\\x9c", "\\x9d", "\\x9e", "\\x9f",
                "\\xa0", "\\xa1", "\\xa2", "\\xa3", "\\xa4", "\\xa5", "\\xa6", "\\xa7", "\\xa8", "\\xa9",
                "\\xaa", "\\xab", "\\xac", "\\xad", "\\xae", "\\xaf",
                "\\xb0", "\\xb1", "\\xb2", "\\xb3", "\\xb4", "\\xb5", "\\xb6", "\\xb7", "\\xb8", "\\xb9",
                "\\xba", "\\xbb", "\\xbc", "\\xbd", "\\xbe", "\\xbf",
                "\\xc0", "\\xc1", "\\xc2", "\\xc3", "\\xc4", "\\xc5", "\\xc6", "\\xc7", "\\xc8", "\\xc9",
                "\\xca", "\\xcb", "\\xcc", "\\xcd", "\\xce", "\\xcf",
                "\\xd0", "\\xd1", "\\xd2", "\\xd3", "\\xd4", "\\xd5", "\\xd6", "\\xd7", "\\xd8", "\\xd9",
                "\\xda", "\\xdb", "\\xdc", "\\xdd", "\\xde", "\\xdf",
                "\\xe0", "\\xe1", "\\xe2", "\\xe3", "\\xe4", "\\xe5", "\\xe6", "\\xe7", "\\xe8", "\\xe9",
                "\\xea", "\\xeb", "\\xec", "\\xed", "\\xee", "\\xef",
                "\\xf0", "\\xf1", "\\xf2", "\\xf3", "\\xf4", "\\xf5", "\\xf6", "\\xf7", "\\xf8", "\\xf9",
                "\\xfa", "\\xfb", "\\xfc", "\\xfd", "\\xfe", "\\xff"
        };
        final var actual = new String[256];
        for (short i = 0; i < actual.length; i++) {
            actual[i] = Utils.toRegexString((byte) i);
        }
        assertArrayEquals(expected, actual);
    }

    @Test
    public void toHumanStringTest() {
        final var expected = new String[]{
                "\\0", "\\x01", "\\x02", "\\x03", "\\x04", "\\x05", "\\x06", "\\x07", "\\x08",
                "\\t", "\\n", "\\x0B", "\\f", "\\r", "\\x0E", "\\x0F", "\\x10", "\\x11", "\\x12", "\\x13", "\\x14",
                "\\x15", "\\x16", "\\x17", "\\x18", "\\x19", "\\x1A", "\\x1B", "\\x1C", "\\x1D", "\\x1E", "\\x1F",
                " ", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z",
                "[", "\\", "]", "^", "_", "`",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
                "s", "t", "u", "v", "w", "x", "y", "z",
                "{", "|", "}", "~", "\\x7F",
                "\\x80", "\\x81", "\\x82", "\\x83", "\\x84", "\\x85", "\\x86", "\\x87", "\\x88", "\\x89",
                "\\x8A", "\\x8B", "\\x8C", "\\x8D", "\\x8E", "\\x8F",
                "\\x90", "\\x91", "\\x92", "\\x93", "\\x94", "\\x95", "\\x96", "\\x97", "\\x98", "\\x99",
                "\\x9A", "\\x9B", "\\x9C", "\\x9D", "\\x9E", "\\x9F",
                "\\xA0", "\\xA1", "\\xA2", "\\xA3", "\\xA4", "\\xA5", "\\xA6", "\\xA7", "\\xA8", "\\xA9",
                "\\xAA", "\\xAB", "\\xAC", "\\xAD", "\\xAE", "\\xAF",
                "\\xB0", "\\xB1", "\\xB2", "\\xB3", "\\xB4", "\\xB5", "\\xB6", "\\xB7", "\\xB8", "\\xB9",
                "\\xBA", "\\xBB", "\\xBC", "\\xBD", "\\xBE", "\\xBF",
                "\\xC0", "\\xC1", "\\xC2", "\\xC3", "\\xC4", "\\xC5", "\\xC6", "\\xC7", "\\xC8", "\\xC9",
                "\\xCA", "\\xCB", "\\xCC", "\\xCD", "\\xCE", "\\xCF",
                "\\xD0", "\\xD1", "\\xD2", "\\xD3", "\\xD4", "\\xD5", "\\xD6", "\\xD7", "\\xD8", "\\xD9",
                "\\xDA", "\\xDB", "\\xDC", "\\xDD", "\\xDE", "\\xDF",
                "\\xE0", "\\xE1", "\\xE2", "\\xE3", "\\xE4", "\\xE5", "\\xE6", "\\xE7", "\\xE8", "\\xE9",
                "\\xEA", "\\xEB", "\\xEC", "\\xED", "\\xEE", "\\xEF",
                "\\xF0", "\\xF1", "\\xF2", "\\xF3", "\\xF4", "\\xF5", "\\xF6", "\\xF7", "\\xF8", "\\xF9",
                "\\xFA", "\\xFB", "\\xFC", "\\xFD", "\\xFE", "\\xFF"
        };
        final var actual = new String[256];
        for (short i = 0; i < actual.length; i++) {
            actual[i] = Utils.toHumanString((byte) i);
        }
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testWildcard() {
        for (char i = 0; i < 256; i++) {
            Assertions.assertTrue(RegexCharacter.PredefinedCharacterClass.WILDCARD.matches((byte) i));
        }
    }
}
