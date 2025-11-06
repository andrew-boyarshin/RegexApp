package syspro.tm;

import java.time.Duration;

final class Statistics {
    public final Duration min;
    public final Duration max;
    public final Duration mean;
    public final Duration stddev;

    public Statistics(long[] measurements, int count) {
        if (count > measurements.length) {
            throw new InternalError(count + " > " + measurements.length);
        }

        if (count == 0) {
            this.min = Duration.ZERO;
            this.max = Duration.ZERO;
            this.mean = Duration.ZERO;
            this.stddev = Duration.ZERO;
            return;
        }

        long total = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (int i = 0; i < count; i++) {
            var duration = measurements[i];
            total += duration;
            if (duration < min) {
                min = duration;
            }
            if (duration > max) {
                max = duration;
            }
        }
        this.min = Duration.ofNanos(min);
        this.max = Duration.ofNanos(max);
        final var mean = total / count;
        this.mean = Duration.ofNanos(mean);

        double totalDeviationSquares = 0;
        for (int i = 0; i < count; i++) {
            var duration = measurements[i];
            final var deviation = duration - mean;
            totalDeviationSquares += Math.pow(deviation, 2);
        }
        this.stddev = Duration.ofNanos(Math.round(Math.sqrt(totalDeviationSquares / (count - 1))));
    }
}
