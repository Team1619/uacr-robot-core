package org.uacr.utilities.services;

/**
 * Keeps track of the timing for services
 */


public class Scheduler {

    private final TimeUnit fTimeUnit;
    private final double fInitialDelay;
    private final double fStandardDelay;

    private long mStartTime = 0;
    private long mLastTime = 0;

    /**
     * @param standardDelay: min time each frame can run
     */
    public Scheduler(double standardDelay) {
        this(0, standardDelay);
    }

    /**
     * @param standardDelay min time each frame can run
     * @param initialDelay delay before first frame
     */
    public Scheduler(double initialDelay, double standardDelay) {
        this(initialDelay, standardDelay, TimeUnit.MILLISECOND);
    }
    /**
     * @param standardDelay min time each frame can run
     * @param timeUnit milliseconds, seconds, minutes
     */
    public Scheduler(double standardDelay, TimeUnit timeUnit) {
        this(0, standardDelay, timeUnit);
    }

    public Scheduler(double initialDelay, double standardDelay, TimeUnit timeUnit) {
        fTimeUnit = timeUnit;
        fInitialDelay = initialDelay;
        fStandardDelay = standardDelay;

        mStartTime = -1;
        mLastTime = -1;
    }

    /**
     * Called on start-up
     */
    public synchronized void start() {
        mStartTime = System.nanoTime();
    }

    /**
     * Called every frame
     */
    public synchronized void run() {
        mStartTime = 0;
        mLastTime = System.nanoTime();
    }

    /**
     * @return true when the service has reached it's min time
     */
    public synchronized boolean shouldRun() {
        long currentTime = System.nanoTime();

        if (mStartTime != 0 && currentTime - mStartTime < fTimeUnit.toNanoseconds(fInitialDelay)) {
            return false;
        }

        return mLastTime == 0 || !(currentTime - mLastTime < fTimeUnit.toNanoseconds(fStandardDelay));
    }

    /**
     * @return the amount of time until the next time the service should run
      */
    public synchronized long nanosUntilNextRun() {
        long currentTime = System.nanoTime();

        long time = nextRunTimeNanos() - currentTime;

        if (time < 0) {
            return 0;
        }

        return time;
    }

    /**
     * @return when to start the next frame
     */
    public synchronized long nextRunTimeNanos() {
        if (mStartTime != 0) {
            long time = (int) (mLastTime + fTimeUnit.toNanoseconds(fInitialDelay));
            if (time < 0) {
                return 0;
            }
            return time;
        }

        long time = (long) (mLastTime + fTimeUnit.toNanoseconds(fStandardDelay));
        if (time < 0) {
            return 0;
        }
        return time;
    }

    /**
     * Stores the conversions to nanoseconds
     * Converts to nanoseconds
     */
    public enum TimeUnit {
        MINUTE(60000000000L),
        SECOND(1000000000),
        MILLISECOND(1000000);

        private long fToNanoseconds;

        TimeUnit(long toNanoseconds) {
            fToNanoseconds = toNanoseconds;
        }

        /**
         * Converts time to nanoseconds
         * @param time the time to convert
         * @return the specified time in nanoseconds
         */
        public synchronized double toNanoseconds(double time) {
            return time * fToNanoseconds;
        }
    }
}
