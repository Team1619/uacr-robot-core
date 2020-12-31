package org.uacr.utilities;

/**
 * Tracks the amount of elapsed time in milliseconds after timer has been started
 * Can be reset and used again
 */
@Deprecated
public class Timer {

    private long mStartTime;
    private long mTime;

    public Timer() {
        mStartTime = -1;
        mTime = 0;
    }

    /**
     * Stores the system time when the timer is started
     * @param timeMs the duration of time to elapse before the timer is complete in milliseconds
     */
    public void start(long timeMs) {
        mTime = timeMs;
        mStartTime = System.currentTimeMillis();
    }

    /**
     * Clears the timer so it can be used again
     */

    public void reset() {
        mStartTime = -1;
    }

    /**
     * @return true if the timer has been started
     */
    public boolean isStarted() {
        return mStartTime > -1;
    }

    /**
     * @return true if the timer has been started and the current system time minus the system time when the timer was started is greater than or equal to the length of time
     * the timer was set for
     */

    public boolean isDone() {
        return mStartTime != -1 && System.currentTimeMillis() - mStartTime >= mTime;
    }
}
