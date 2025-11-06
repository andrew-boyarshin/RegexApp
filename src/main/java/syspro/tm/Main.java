package syspro.tm;

import syspro.tm.RunnerThread.Job;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

public final class Main {

    static final int ITERATION_COUNT_MAX = 100_000_000;
    private static final TimeUnit[] TIME_UNITS = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS};
    private static Job lastJob = null;
    private static String[] args;

    static void main(String... args) {
        Thread.currentThread().setName("Main Thread (Terminal UI Thread)");
        Main.args = args;

        if (Configuration.implementations().isEmpty()) {
            IO.println("No RegexEngine implementations, nothing to do.");
            System.exit(1);
        }

        if (Configuration.internalTestData().isEmpty()) {
            IO.println("No test data, nothing to do.");
            System.exit(1);
        }

        if (!hasArg("--no-tests")) {
            runTests();
        }

        var shouldPrintProgress = shouldPrintProgress();

        // Progress
        final var runnerThread = new RunnerThread();
        runnerThread.start();
        while (runnerThread.isAlive()) {
            if (shouldPrintProgress) {
                printStatus(runnerThread);
            }
            try {
                runnerThread.join(200);
            } catch (InterruptedException _) {
            }
        }
        if (shouldPrintProgress) {
            printStatus(runnerThread);
            IO.println();
        }

        for (final var implementation : Configuration.implementations()) {
            IO.println(implementation.engineName + ":");

            var min = ChronoUnit.FOREVER.getDuration();
            for (final var result : implementation.benchmarkResults) {
                final var statistics = result.statistics();
                if (statistics.min.compareTo(min) < 0) {
                    min = statistics.min;
                }
            }

            final var minUnit = bestTimeUnit(min);
            final var unitName = timeUnitShortName(minUnit);

            int caseIndex = 1;
            for (final var result : implementation.benchmarkResults) {
                final var statistics = result.statistics();
                IO.print("Benchmark " + caseIndex + ": ");
                if (result.failedIteration != null) {
                    IO.print("failed ");
                    if (result.failedIteration != 0) {
                        IO.print("at iteration " + result.failedIteration + ' ');
                    }
                    if (result.testData.slidingWindowSize == null) {
                        IO.print("(expected " + (result.testData.expected ? "" : "non-") + "match) ");
                    }
                }

                IO.print(format(statistics.mean, minUnit));
                if (result.iterations == 1) {
                    IO.println(' ' + unitName + " (1 iteration)");
                } else {
                    IO.println(" ± " + format(statistics.stddev, minUnit) + "  [" + format(statistics.min, minUnit) + " … " + format(statistics.max, minUnit) + "] " + unitName + " (" + result.iterations + " iterations)");
                }
                caseIndex++;
            }
        }
    }

    private static boolean hasArg(String target) {
        for (final var arg : args) {
            if (arg.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private static void runTests() {
        final var testCases = Configuration.testCases();
        if (testCases.isEmpty()) {
            return;
        }

        final var testCaseCount = testCases.size();

        for (final var implementation : Configuration.implementations()) {
            final var engine = implementation.engine;
            final var engineName = implementation.engineName;

            final var iterator = testCases.listIterator();
            int failures = 0;
            var totalDuration = Duration.ZERO;
            while (iterator.hasNext()) {
                final var index = iterator.nextIndex() + 1;
                final var testData = iterator.next();
                IO.print('\r' + engineName + ": Running test " + index + '/' + testCaseCount + "... ");
                final var regex = testData.regex.clone();
                final var input = testData.input.clone();
                final var start = System.nanoTime();
                final var actual = engine.matches(regex, input);
                final var time = System.nanoTime() - start;
                totalDuration = totalDuration.plus(Duration.ofNanos(time));
                final var failed = actual != testData.expected;
                IO.print('\r' + engineName + ": Test " + index + '/' + testCaseCount);
                if (failed) {
                    failures++;
                    IO.println(" has failed, expected " + (testData.expected ? "" : "non-") + "match.");
                } else {
                    IO.print(" has succeeded. ");
                }
            }

            IO.println();

            final var minUnit = bestTimeUnit(totalDuration);
            final var unitName = timeUnitShortName(minUnit);
            IO.print(engineName + ": ");
            if (failures != testCaseCount) {
                IO.print((testCaseCount - failures) + " succeeded");
                if (failures != 0) {
                    IO.print(" & ");
                }
            }
            if (failures != 0) {
                IO.print(failures + " failed");
            }
            IO.println(" in " + format(totalDuration, minUnit) + ' ' + unitName);
        }
    }

    private static boolean shouldPrintProgress() {
        for (final var configurationProvider : Configuration.configurationProviders()) {
            if (configurationProvider.shouldDisableProgressUpdates()) {
                return false;
            }
        }
        return true;
    }

    private static TimeUnit bestTimeUnit(Duration min) {
        var minUnit = TimeUnit.NANOSECONDS;
        for (final var unitCandidate : TIME_UNITS) {
            final var minConverted = unitCandidate.convert(min);
            if (minConverted != 0) {
                minUnit = unitCandidate;
                break;
            }
        }
        return minUnit;
    }

    private static void printStatus(RunnerThread runnerThread) {
        final var lastJob = Main.lastJob;
        final var lastJobResults = lastJob != null ? lastJob.results : null;
        final var currentJob = runnerThread.currentJob;
        final var currentJobResults = currentJob != null ? currentJob.results : null;
        Main.lastJob = currentJob;

        if (lastJobResults != null) {
            if (currentJobResults == null || currentJobResults.implementation != lastJobResults.implementation) {
                final var implementation = lastJobResults.implementation.engineName;
                final var failedIteration = lastJobResults.failedIteration;
                IO.println("\rBenchmarking " + implementation + ' ' + (failedIteration == null ? "done" : "failed") + '.');
            }
        }

        if (currentJobResults != null) {
            final var implementation = currentJobResults.implementation.engineName;
            final var caseIndex = currentJobResults.index;
            final var singleSlice = 1.0 / Configuration.benchmarkCases().size();
            final var startTime = currentJob.startTime;
            final var deadline = currentJob.deadline;
            final var elapsed = Math.max(0, System.nanoTime() - startTime);
            final var total = Math.max(deadline - startTime, elapsed);
            final var percentage = (caseIndex * singleSlice) + (elapsed * singleSlice / total);
            IO.print("\rBenchmarking %s: %.1f%% ".formatted(implementation, percentage * 100.0));
        }
    }

    private static String format(Duration duration, TimeUnit unit) {
        return Long.toString(unit.convert(duration));
    }

    private static String timeUnitShortName(TimeUnit unit) {
        return switch (unit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "us";
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "min";
            case HOURS -> "hr";
            case DAYS -> "day";
        };
    }
}
