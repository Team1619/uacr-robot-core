package org.uacr.utilities;

public class Timer {

    private long mStartTime = -1;
    private long mTime;

    public void start(long timeMs) {
        this.mTime = timeMs;
        this.mStartTime = System.currentTimeMillis();
    }

    public void reset() {
        this.mStartTime = -1;
    }

    public boolean isStarted() {
        return this.mStartTime > -1;
    }

    public boolean isDone() {
        return mStartTime != -1 && System.currentTimeMillis() - mStartTime >= mTime;
    }
}
