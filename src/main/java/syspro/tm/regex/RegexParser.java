package syspro.tm.regex;

import syspro.tm.Utils;

import java.util.ArrayList;

final class RegexParser {
    private final byte[] text;
    private int position;

    public RegexParser(byte[] text) {
        this.text = text;
    }

    private void consume(byte expected) {
        if (position >= text.length) {
            throw new RegexSyntaxException(text, position, "Unexpected end of regular expression, expected " + format(expected));
        }
        final var c = peek();
        if (c == expected) {
            position++;
        } else {
            throw new RegexSyntaxException(text, position, "Expected " + format(expected) + " but got " + format(c));
        }
    }

    private void consume(char expected) {
        if (expected >= 256) {
            throw new InternalError("Non-byte expected value: " + ((int) expected));
        }
        consume((byte) expected);
    }

    private byte peek() {
        if (position >= text.length) {
            throw new RegexSyntaxException(text, position, "Unexpected end of regular expression");
        }
        return text[position];
    }

    private boolean peekIsAny(byte... options) {
        final var c = peek();
        for (final var option : options) {
            if (c == option) {
                return true;
            }
        }
        return false;
    }

    private boolean peekIsAny(char... options) {
        final int optionsLength = options.length;
        final var newOptions = new byte[optionsLength];
        for (int i = 0; i < optionsLength; i++) {
            final var option = options[i];
            if (option >= 256) {
                throw new InternalError("Non-byte alternative: " + ((int) option));
            }
            newOptions[i] = (byte) option;
        }
        return peekIsAny(newOptions);
    }

    private byte advance() {
        return text[position++];
    }

    public boolean hasMore() {
        return position + 1 <= text.length;
    }

    private String format(byte c) {
        return Utils.toHumanString(c);
    }

    public Regex regex() {
        return alternation();
    }

    private Regex alternation() {
        final var node = concatenation();

        if (hasMore() && peekIsAny('|')) {
            final var alternatives = new ArrayList<Regex>();
            alternatives.add(node);
            while (hasMore() && peekIsAny('|')) {
                consume('|');
                alternatives.add(concatenation());
            }
            return new Regex.Alternation(alternatives.toArray(Regex[]::new));
        }

        return node;
    }

    private Regex concatenation() {
        if (!hasMore() || hasMore() && peekIsAny('|', ')')) {
            return new Regex.Concatenation();
        }

        final var node = repetition();

        if (hasMore() && !peekIsAny('|', ')')) {
            final var parts = new ArrayList<Regex>();
            parts.add(node);
            while (hasMore() && !peekIsAny('|', ')')) {
                parts.add(repetition());
            }
            return new Regex.Concatenation(parts.toArray(Regex[]::new));
        }

        return node;
    }

    private Regex repetition() {
        var atomNode = atom();
        while (hasMore() && peekIsAny('*', '+', '?', '{')) {
            var character = peek();
            switch (character) {
                case '*':
                    consume('*');
                    atomNode = new Regex.Repetition(atomNode, 0, null);
                    break;
                case '+':
                    consume('+');
                    atomNode = new Regex.Repetition(atomNode, 1, null);
                    break;
                case '?':
                    consume('?');
                    atomNode = new Regex.Repetition(atomNode, 0, 1);
                    break;
                case '{':
                    consume('{');
                    atomNode = repetitionRange(atomNode);
                    consume('}');
                    break;
            }
        }
        return atomNode;
    }

    private Regex.Repetition repetitionRange(Regex argument) {
        Integer min = null;
        Integer max = null;
        if (hasMore() && Character.isDigit(peek())) {
            min = repetitionNumber();
        }
        if (peekIsAny(',')) {
            consume(',');
            if (hasMore() && Character.isDigit(peek())) {
                max = repetitionNumber();
            }
        } else {
            max = min;
        }
        return new Regex.Repetition(argument, min, max);
    }

    private Integer repetitionNumber() {
        final var sb = new StringBuilder();
        while (hasMore() && Character.isDigit(peek())) {
            sb.append((char) advance());
        }
        try {
            return Integer.parseUnsignedInt(sb.toString());
        } catch (NumberFormatException e) {
            throw new RegexSyntaxException(text, position, "Invalid decimal number for repetition count: " + sb);
        }
    }

    private Regex atom() {
        switch (peek()) {
            case '(': {
                consume('(');
                var regexNode = regex();
                consume(')');
                return regexNode;
            }
            case '\\': {
                consume('\\');
                return new Regex.SingleCharacter(escapedSpecialCharacter(advance()));
            }
            case '.': {
                consume('.');
                return new Regex.SingleCharacter(RegexCharacter.PredefinedCharacterClass.WILDCARD);
            }
            case '[': {
                consume('[');
                var characterClassNode = characterClass();
                consume(']');
                return new Regex.SingleCharacter(characterClassNode);
            }
            case ')': {
                throw new RegexSyntaxException(text, position, "Unexpected group closing parenthesis");
            }
            case ']': {
                throw new RegexSyntaxException(text, position, "Unexpected character class closing bracket");
            }
            case '+', '*', '?': {
                throw new RegexSyntaxException(text, position, "Unexpected repetition operator");
            }
            case '|': {
                throw new RegexSyntaxException(text, position, "Unexpected alternation operator");
            }
            default: {
                return new Regex.SingleCharacter(new RegexCharacter.Constant(advance()));
            }
        }
    }

    private RegexCharacter escapedSpecialCharacter(byte character) {
        return switch (character) {
            case 'w' -> RegexCharacter.PredefinedCharacterClass.WORD;
            case 'W' -> RegexCharacter.PredefinedCharacterClass.NON_WORD;
            case 'd' -> RegexCharacter.PredefinedCharacterClass.DIGIT;
            case 'D' -> RegexCharacter.PredefinedCharacterClass.NON_DIGIT;
            case 's' -> RegexCharacter.PredefinedCharacterClass.SPACE;
            case 'S' -> RegexCharacter.PredefinedCharacterClass.NON_SPACE;
            default -> new RegexCharacter.Constant(character);
        };
    }

    private RegexCharacter characterClass() {
        final var negative = hasMore() && peekIsAny('^');
        if (negative) {
            consume('^');
        }

        final var classes = new ArrayList<RegexCharacter>();
        while (hasMore() && peek() != ']') {
            classes.add(singleCharacterClassElement());
        }

        if (classes.isEmpty()) {
            throw new RegexSyntaxException(text, position, "Character class should have at least one item");
        }

        RegexCharacter output;
        if (classes.size() == 1) {
            output = classes.getFirst();
        } else {
            output = new RegexCharacter.Union(classes.toArray(new RegexCharacter[0]));
        }

        if (negative) {
            output = new RegexCharacter.Negation(output);
        }

        return output;
    }

    private RegexCharacter singleCharacterClassElement() {
        if (peekIsAny('[')) {
            // Nested character class
            consume('[');
            var nestedCharacterClass = characterClass();
            consume(']');
            return nestedCharacterClass;
        }

        var characterStart = advance();
        if (characterStart == '\\') {
            return escapedSpecialCharacter(advance());
        }

        if (peekIsAny('-')) {
            // We've got a range
            consume('-');

            var characterEnd = advance();

            if (characterEnd == '\\') {
                characterEnd = advance();
                if (characterEnd != '\\') {
                    throw new RegexSyntaxException(text, position - 1, "Unexpected range ending escape sequence for character class: " + format(characterEnd));
                }
            }

            return new RegexCharacter.Range(characterStart, characterEnd);
        }

        return new RegexCharacter.Constant(characterStart);
    }
}
