package syspro.tm.regex;

public final class RegexSyntaxException extends RuntimeException {
    private final byte[] regex;
    private final int position;

    public RegexSyntaxException(byte[] regex, int position, String message) {
        super(message);
        this.regex = regex;
        this.position = position;
    }

    public byte[] regex() {
        return regex.clone();
    }

    public int position() {
        return position;
    }
}
