package org.uacr.utilities;

import org.uacr.shared.abstractions.InputValues;

/**
 * Tracks the amount of elapsed time in milliseconds after timer has been started
 * Can be reset and used again
 */

public class RobotTimer {

    private double mStartTime;
    private double mDuration;

    private final InputValues fSharedInputValues;

    public RobotTimer(InputValues inputValues) {
        mStartTime = -1;
        mDuration = 0;

        fSharedInputValues = inputValues;
    }

    /**
     * Stores the system time when the timer is started
     * @param durationMs the duration of time to elapse before the timer is complete in milliseconds
     */
    public void start(long durationMs) {
        mDuration = durationMs;
        mStartTime = fSharedInputValues.getNumeric("ipn_frame_start_time");
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
        return isStarted() && fSharedInputValues.getNumeric("ipn_frame_start_time") - mStartTime >= mDuration;
    }
}
