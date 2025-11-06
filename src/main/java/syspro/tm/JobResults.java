package syspro.tm;

import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicReference;

final class JobResults {
    public final Implementation implementation;
    public final TestData testData;
    public final int index;
    public long[] measurements;
    public volatile int iterations;
    public volatile Integer failedIteration;
    private final AtomicReference<Statistics> statistics = new AtomicReference<>();

    public JobResults(Implementation implementation, TestData testData, int index) {
        this.implementation = implementation;
        this.testData = testData;
        this.index = index;
        implementation.benchmarkResults.add(this);
        measurements = new long[testData.slidingWindowSize == null ? Main.ITERATION_COUNT_MAX : 1];
    }

    public Statistics statistics() {
        var statistics = this.statistics.get();
        if (statistics == null) {
            statistics = computeStatistics();
            if (this.statistics.compareAndSet(null, statistics)) {
                measurements = null;
            } else {
                statistics = this.statistics.get();
            }
            return statistics;
        }
        return statistics;
    }

    private Statistics computeStatistics() {
        VarHandle.fullFence();
        return new Statistics(measurements, iterations);
    }
}
