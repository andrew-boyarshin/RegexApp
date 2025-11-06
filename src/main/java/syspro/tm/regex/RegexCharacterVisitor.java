package syspro.tm.regex;

public interface RegexCharacterVisitor<R> {
    default R visitDefault(RegexCharacter node) {
        return null;
    }

    default R visitConstant(RegexCharacter.Constant node) {
        return visitDefault(node);
    }

    default R visitRange(RegexCharacter.Range node) {
        return visitDefault(node);
    }

    default R visitUnion(RegexCharacter.Union node) {
        return visitDefault(node);
    }

    default R visitNegation(RegexCharacter.Negation node) {
        return visitDefault(node);
    }

    default R visitPredefinedCharacterClass(RegexCharacter.PredefinedCharacterClass node) {
        return visitDefault(node);
    }
}
