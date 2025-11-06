package syspro.tm;

public final class TestData {
    public final ConfigurationProvider provider;
    public final byte[] regex;
    public final byte[] input;
    public final boolean expected;
    public final boolean benchmark;
    public final Integer slidingWindowSize;
    private static final int MAX_INPUT_LENGTH = 20;

    TestData(ConfigurationProvider provider, byte[] regex, byte[] input, boolean expected, boolean benchmark, Integer slidingWindowSize) {
        this.provider = provider;
        this.regex = regex;
        this.input = input;
        this.expected = expected;
        this.benchmark = benchmark;
        this.slidingWindowSize = slidingWindowSize;
    }

    TestData publicClone() {
        return new TestData(this.provider, this.regex.clone(), this.input.clone(), this.expected, this.benchmark, this.slidingWindowSize);
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        if (!(provider instanceof DefaultTestCases)) {
            sb.append('[');
            sb.append(provider.getClass().getSimpleName());
            sb.append("] ");
        }
        sb.append("regex=");
        for (final var b : regex) {
            sb.append(Utils.toHumanString(b));
        }
        sb.append(",input=");
        int inputLength = input.length;
        for (int i = 0; i < Math.min(inputLength, MAX_INPUT_LENGTH); i++) {
            sb.append(Utils.toHumanString(input[i]));
        }
        if (inputLength > MAX_INPUT_LENGTH) {
            sb.append("... +");
            sb.append(inputLength - MAX_INPUT_LENGTH);
            sb.append(" more");
        }
        if (slidingWindowSize != null) {
            sb.append(",slidingWindowSize=");
            sb.append(slidingWindowSize);
        } else {
            sb.append(benchmark ? ",benchmark" : ",test");
            sb.append(expected ? ",positive" : ",negative");
        }
        return sb.toString();
    }
}
