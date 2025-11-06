package syspro.tm;

import java.util.Arrays;

final class RunnerThread extends Thread {
    private static final int REGEX_CLONE_FREQUENCY = 100_000;
    volatile Job currentJob;

    public RunnerThread() {
        super("Benchmark Thread");
    }

    @Override
    public void run() {
        for (final var implementation : Configuration.implementations()) {
            final var iterator = Configuration.benchmarkCases().listIterator();
            while (iterator.hasNext()) {
                final var index = iterator.nextIndex();
                final var testData = iterator.next();
                final var results = new JobResults(implementation, testData, index);
                final var job = new Job(results);
                currentJob = job;
                if (testData.slidingWindowSize != null) {
                    measureSlidingWindow(job);
                } else {
                    measure(job);
                }
            }
            currentJob = null;
        }
    }

    private void measure(Job job) {
        final var results = job.results;
        final var engine = results.implementation.engine;
        final var testCase = results.testData;
        var regex = testCase.regex.clone();
        final var input = testCase.input;
        final var expected = testCase.expected;
        final var measurements = results.measurements;
        long time;
        long deadline = job.deadline;
        int i = 0;
        var ok = true;
        do {
            final var start = System.nanoTime();
            try {
                final var actual = engine.matches(regex, input);
                if (actual != expected) {
                    ok = false;
                    break;
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                ok = false;
                break;
            } finally {
                time = System.nanoTime();
            }
            final var duration = time - start;
            measurements[i++] = duration;
            if (i % REGEX_CLONE_FREQUENCY == 0) {
                regex = testCase.regex.clone();
            }
        } while (time < deadline && i != Main.ITERATION_COUNT_MAX);
        if (!ok) {
            results.failedIteration = i;
        }
        results.iterations = i;
        results.statistics();
    }

    boolean actualBox;

    private void measureSlidingWindow(Job job) {
        final var results = job.results;
        final var engine = results.implementation.engine;
        final var testCase = results.testData;
        var regex = testCase.regex.clone();
        final var input = testCase.input;
        final var measurements = results.measurements;
        final var windowSize = testCase.slidingWindowSize;
        long time = 0;
        var ok = true;
        for (int i = 0; i < input.length; i += windowSize) {
            final var window = Arrays.copyOfRange(input, i, i + windowSize);
            final var start = System.nanoTime();
            final boolean actual;
            try {
                actual = engine.matches(regex, window);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                ok = false;
                break;
            } finally {
                time += System.nanoTime() - start;
            }
            actualBox = actual;
        }
        measurements[0] = time;
        if (!ok) {
            results.failedIteration = 1;
        }
        results.iterations = 1;
        results.statistics();
    }

    static final class Job {
        final JobResults results;
        final long startTime;
        final long deadline;

        Job(JobResults results) {
            this.results = results;

            final var time = System.nanoTime();
            final var deadline = time + 10_000_000_000L;
            this.startTime = time;
            this.deadline = deadline;
        }
    }
}
