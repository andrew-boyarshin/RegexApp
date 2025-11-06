package syspro.tm.regex;

import syspro.tm.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A regular expression pattern to match a single character of byte input.
 */
public sealed abstract class RegexCharacter {
    public abstract boolean matches(byte b);

    public abstract <R> R accept(RegexCharacterVisitor<R> visitor);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    public static final class Constant extends RegexCharacter {
        public final byte value;

        public Constant(byte value) {
            this.value = value;
        }

        public Constant(char value) {
            if (value >= 256) {
                throw new IllegalArgumentException("value: " + ((int) value) + " >= 256");
            }
            this((byte) value);
        }

        @Override
        public boolean matches(byte b) {
            return b == value;
        }

        @Override
        public <R> R accept(RegexCharacterVisitor<R> visitor) {
            return visitor.visitConstant(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Constant constant && value == constant.value;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public String toString() {
            return Utils.toRegexString(value);
        }
    }

    public static final class Range extends RegexCharacter {
        public final byte start;
        public final byte end;

        /**
         * @param start Range start character, inclusive
         * @param end   Range end character, inclusive
         */
        public Range(byte start, byte end) {
            if (Byte.compareUnsigned(start, end) > 0) {
                this.start = end;
                this.end = start;
            } else {
                this.start = start;
                this.end = end;
            }
        }

        /**
         * @param start Range start character, inclusive
         * @param end   Range end character, inclusive
         */
        public Range(char start, char end) {
            if (start >= 256) {
                throw new IllegalArgumentException("start: " + ((int) start) + " >= 256");
            }
            if (end >= 256) {
                throw new IllegalArgumentException("end: " + ((int) end) + " >= 256");
            }
            this((byte) start, (byte) end);
        }

        @Override
        public boolean matches(byte b) {
            return Byte.compareUnsigned(start, b) <= 0 && Byte.compareUnsigned(b, end) <= 0;
        }

        @Override
        public <R> R accept(RegexCharacterVisitor<R> visitor) {
            return visitor.visitRange(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Range range && start == range.start && end == range.end;
        }

        @Override
        public int hashCode() {
            return 257 * start + end;
        }

        private String toShortString() {
            return Utils.toRegexString(start) + '-' + Utils.toRegexString(end);
        }

        @Override
        public String toString() {
            return '[' + toShortString() + ']';
        }
    }

    public static final class Union extends RegexCharacter implements Iterable<RegexCharacter> {
        private final RegexCharacter[] characters;

        public Union(RegexCharacter... characters) {
            this.characters = characters;
        }

        public RegexCharacter[] characters() {
            return characters.clone();
        }

        @Override
        public boolean matches(byte b) {
            for (final var character : characters) {
                if (character.matches(b)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public <R> R accept(RegexCharacterVisitor<R> visitor) {
            return visitor.visitUnion(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Union union && Arrays.equals(characters, union.characters);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(characters);
        }

        private static void toRegexString(List<String> strings, RegexCharacter... characters) {
            for (final var character : characters) {
                if (character instanceof Union union) {
                    toRegexString(strings, union.characters);
                    continue;
                }

                if (character instanceof Range range) {
                    strings.add(range.toShortString());
                    continue;
                }

                strings.add(character.toString());
            }
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            final var parts = new ArrayList<String>();
            toRegexString(parts, characters);
            sb.append('[');
            for (final var part : parts) {
                sb.append(part);
            }
            sb.append(']');
            return sb.toString();
        }

        @Override
        public Iterator<RegexCharacter> iterator() {
            return Arrays.asList(characters).iterator();
        }
    }

    public static final class Negation extends RegexCharacter {
        public final RegexCharacter argument;

        public Negation(RegexCharacter argument) {
            if (argument == null) {
                throw new IllegalArgumentException("Negation argument cannot be null");
            }

            this.argument = argument;
        }

        @Override
        public boolean matches(byte b) {
            return !argument.matches(b);
        }

        @Override
        public <R> R accept(RegexCharacterVisitor<R> visitor) {
            return visitor.visitNegation(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Negation negation && argument.equals(negation.argument);
        }

        @Override
        public int hashCode() {
            return argument.hashCode();
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            final var parts = new ArrayList<String>();
            if (argument instanceof Union union) {
                Union.toRegexString(parts, union.characters);
            } else {
                Union.toRegexString(parts, argument);
            }
            sb.append("[^");
            for (final var part : parts) {
                sb.append(part);
            }
            sb.append(']');
            return sb.toString();
        }
    }

    public static final class PredefinedCharacterClass extends RegexCharacter {
        public static final PredefinedCharacterClass WORD = new PredefinedCharacterClass("\\w", new Union(new Range('a', 'z'), new Range('A', 'Z'), new Constant('_')));
        public static final PredefinedCharacterClass NON_WORD = new PredefinedCharacterClass("\\W", new Negation(WORD.character));
        public static final PredefinedCharacterClass DIGIT = new PredefinedCharacterClass("\\d", new Range('0', '9'));
        public static final PredefinedCharacterClass NON_DIGIT = new PredefinedCharacterClass("\\D", new Negation(DIGIT.character));
        public static final PredefinedCharacterClass SPACE = new PredefinedCharacterClass("\\s", new Union(new Constant(' '), new Constant('\r'), new Constant('\n'), new Constant('\f'), new Constant('\t'), new Constant((byte) 0x0B)));
        public static final PredefinedCharacterClass NON_SPACE = new PredefinedCharacterClass("\\S", new Negation(SPACE.character));
        public static final PredefinedCharacterClass WILDCARD = new PredefinedCharacterClass(".", new Range((char) 0, (char) 255));

        public final String mnemonic;
        public final RegexCharacter character;

        private PredefinedCharacterClass(String mnemonic, RegexCharacter character) {
            this.mnemonic = mnemonic;
            this.character = character;
        }

        @Override
        public boolean matches(byte b) {
            return character.matches(b);
        }

        @Override
        public <R> R accept(RegexCharacterVisitor<R> visitor) {
            return visitor.visitPredefinedCharacterClass(this);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public String toString() {
            return mnemonic;
        }
    }
}
