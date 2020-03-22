package org.uacr.utilities;

public class Timer {

    private long mStartTime;
    private long mTime;

    public Timer() {
        mStartTime = -1;
        mTime = 0;
    }

    public void start(long timeMs) {
        mTime = timeMs;
        mStartTime = System.currentTimeMillis();
    }

    public void reset() {
        mStartTime = -1;
    }

    public boolean isStarted() {
        return mStartTime > -1;
    }

    public boolean isDone() {
        return mStartTime != -1 && System.currentTimeMillis() - mStartTime >= mTime;
    }
}
