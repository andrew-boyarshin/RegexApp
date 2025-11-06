package syspro.tm.regex;

public interface RegexVisitor<R> {
    default R visitDefault(Regex node) {
        return null;
    }

    default R visitSingleCharacter(Regex.SingleCharacter node) {
        return visitDefault(node);
    }

    default R visitAlternation(Regex.Alternation node) {
        return visitDefault(node);
    }

    default R visitConcatenation(Regex.Concatenation node) {
        return visitDefault(node);
    }

    default R visitRepetition(Regex.Repetition node) {
        return visitDefault(node);
    }
}
