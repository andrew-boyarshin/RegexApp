package syspro.tm.regex;

import syspro.tm.Utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

/**
 * Parsed regular expression pattern that operates on {@link RegexCharacter}.
 */
public sealed abstract class Regex {
    public static Regex parse(byte[] text) {
        return new RegexParser(text).regex();
    }

    public static Regex parse(String text) {
        return parse(Utils.toBytes(text));
    }

    public abstract <R> R accept(RegexVisitor<R> visitor);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    public static final class SingleCharacter extends Regex {
        public final RegexCharacter character;

        public SingleCharacter(RegexCharacter character) {
            if (character == null) {
                throw new IllegalArgumentException("Regex should have non-null argument character");
            }

            this.character = character;
        }

        @Override
        public <R> R accept(RegexVisitor<R> visitor) {
            return visitor.visitSingleCharacter(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SingleCharacter that && character.equals(that.character);
        }

        @Override
        public int hashCode() {
            return character.hashCode();
        }

        @Override
        public String toString() {
            return character.toString();
        }
    }

    public static final class Alternation extends Regex implements Iterable<Regex> {
        private final Regex[] options;

        public Alternation(Regex... options) {
            if (options == null || options.length == 0) {
                throw new IllegalArgumentException("Alternation should have at least 1 option");
            }

            this.options = options;
        }

        public Regex get(int index) {
            return options[index];
        }

        public int size() {
            return options.length;
        }

        @Override
        public <R> R accept(RegexVisitor<R> visitor) {
            return visitor.visitAlternation(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Alternation regexes && Arrays.equals(options, regexes.options);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(options);
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            for (final var part : options) {
                if (!sb.isEmpty()) {
                    sb.append('|');
                }

                if (part != null) {
                    sb.append(part);
                }
            }
            return sb.toString();
        }

        @Override
        public Iterator<Regex> iterator() {
            return Arrays.asList(options).iterator();
        }
    }

    public static final class Concatenation extends Regex implements Iterable<Regex> {
        private final Regex[] parts;

        public Concatenation(Regex... parts) {
            if (parts == null) {
                throw new IllegalArgumentException("Concatenation should have non-null parts");
            }

            this.parts = parts;
        }

        public Regex get(int index) {
            return parts[index];
        }

        public int size() {
            return parts.length;
        }

        @Override
        public <R> R accept(RegexVisitor<R> visitor) {
            return visitor.visitConcatenation(this);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Concatenation that && Arrays.equals(parts, that.parts);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(parts);
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            for (final var part : parts) {
                if (part != null) {
                    final var parenthesized = part instanceof Regex.Alternation;
                    if (parenthesized) {
                        sb.append('(');
                    }
                    sb.append(part);
                    if (parenthesized) {
                        sb.append(')');
                    }
                }
            }
            return sb.toString();
        }

        @Override
        public Iterator<Regex> iterator() {
            return Arrays.asList(parts).iterator();
        }
    }

    public static final class Repetition extends Regex {
        public final Regex argument;
        public final Integer min;
        public final Integer max;

        /**
         * @param argument Regex pattern to be repeated
         * @param min      If non-{@code null}, pattern must be repeated at least {@code min} times
         * @param max      If non-{@code null}, pattern must be repeated no more than {@code max} times
         */
        public Repetition(Regex argument, Integer min, Integer max) {
            if (argument == null) {
                throw new IllegalArgumentException("Repetition should have non-null argument");
            }

            if (min != null && min < 0) {
                throw new IllegalArgumentException("Min should not be negative: " + min);
            }

            if (max != null && max <= 0) {
                throw new IllegalArgumentException("Max should be positive: " + max);
            }

            if (min != null && max != null && min > max) {
                throw new IllegalArgumentException("Invalid repetition count range: " + min + " > " + max);
            }

            if (min == null && max == null) {
                throw new IllegalArgumentException("Min and max cannot both be null");
            }

            this.argument = argument;
            this.min = min;
            this.max = max;
        }

        @Override
        public <R> R accept(RegexVisitor<R> visitor) {
            return visitor.visitRepetition(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Repetition that) {
                return argument.equals(that.argument) && Objects.equals(min, that.min) && Objects.equals(max, that.max);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(max);
            result = 31 * result + Objects.hashCode(min);
            return 31 * result + argument.hashCode();
        }

        @Override
        public String toString() {
            final var sb = new StringBuilder();
            final var parenthesized = !(argument instanceof SingleCharacter);
            if (parenthesized) {
                sb.append('(');
            }
            sb.append(argument);
            if (parenthesized) {
                sb.append(')');
            }
            if (min == null) {
                return sb.append("{,").append(max).append('}').toString();
            }
            if (max == null) {
                if (min == 0) {
                    sb.append('*');
                } else if (min == 1) {
                    sb.append('+');
                } else {
                    sb.append('{').append(min).append(",}");
                }
                return sb.toString();
            }
            if (min == 0 && max == 1) {
                return sb.append('?').toString();
            }
            return sb.append('{').append(min).append(',').append(max).append('}').toString();
        }
    }
}
