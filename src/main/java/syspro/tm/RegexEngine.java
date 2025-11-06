package syspro.tm;

public interface RegexEngine {
    /**
     * Match the given regex against the entire given input.
     */
    boolean matches(byte[] regex, byte[] input);
}
