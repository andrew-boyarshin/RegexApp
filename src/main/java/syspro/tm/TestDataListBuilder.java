package syspro.tm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public abstract sealed class TestDataListBuilder permits Configuration.TestDataListBuilderImpl {
    protected Group currentGroup;

    public final Group testGroup() {
        return new Group(false);
    }

    public final Group benchmarkGroup() {
        return new Group(true);
    }

    public abstract void add(byte[] regex, byte[] input, boolean expected);

    public final void add(String regex, String input, boolean expected) {
        add(Utils.toBytes(regex), Utils.toBytes(input), expected);
    }

    public final void add(String regex, File input, boolean expected) {
        byte[] inputBytes;
        try {
            inputBytes = Files.readAllBytes(input.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        add(Utils.toBytes(regex), inputBytes, expected);
    }

    public abstract void addSlidingWindowBenchmark(byte[] regex, byte[] input, int inputWindow);

    public final class Group implements AutoCloseable {
        public final boolean isBenchmark;
        private final Group outer;
        private final IllegalStateException allocation;
        private IllegalStateException closed;

        private Group(boolean isBenchmark) {
            this.isBenchmark = isBenchmark;
            this.allocation = new IllegalStateException("Group is not closed (use try-with-resources)");

            this.outer = currentGroup;
            currentGroup = this;
        }

        @Override
        public void close() {
            if (closed != null) {
                throw closed;
            }

            if (currentGroup == this) {
                currentGroup = outer;
                closed = new IllegalStateException("Group is already closed");
                return;
            }

            final var otherAllocation = currentGroup.allocation;
            otherAllocation.initCause(allocation);
            throw new IllegalStateException("Group nesting is invalid", otherAllocation);
        }
    }
}