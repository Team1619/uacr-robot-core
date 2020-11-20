package org.uacr.utilities.services;

import javax.annotation.Nullable;

/**
 * Manages one service with or without a scheduler
 */

public class ServiceWrapper implements Service {

    private final Service fService;

    @Nullable
    private final Scheduler scheduler;
    @Nullable
    private ServiceState mServiceState;
    private boolean mIsCurrentlyRunning;

    public ServiceWrapper(Service service) {
        fService = service;

        // Gets a scheduler if it is a scheduled service
        if (fService instanceof ScheduledService) {
            scheduler = ((ScheduledService) fService).scheduler();
        } else {
            scheduler = null;
        }

        mServiceState = ServiceState.AWAITING_START;
        mIsCurrentlyRunning = false;
    }

    // Returns the current state of the service
    public ServiceState getServiceState() {
        synchronized (mServiceState) {
            return mServiceState;
        }
    }

    /**
     * Determines whether a service should run based on the service's current state and a scheduler if included in the service
     */
    public boolean shouldRun() {
        synchronized (mServiceState) {
            if (mServiceState == ServiceState.AWAITING_START || mServiceState == ServiceState.STOPPING) {
                return false;
            }
        }

        // Do nothing if runOneIteration in the service is currently running
        if (isCurrentlyRunning()) {
            return false;
        }

        if (scheduler != null) {
            return scheduler.shouldRun();
        }

        return true;
    }

    public long nanosUntilNextRun() {
        if (scheduler != null) {
            return scheduler.nanosUntilNextRun();
        }

        return 0;
    }

    /**
     * @return the time until the next cycle should start
     */
    public long nextRunTimeNanos() {
        if (isCurrentlyRunning()) {
            return Long.MAX_VALUE;
        }


        if (scheduler != null) {
            return scheduler.nextRunTimeNanos();
        }

        return 0;
    }

    /**
     * @return true if a the service is running
     */
    public boolean isCurrentlyRunning() {
        return mIsCurrentlyRunning;
    }

    /**
     * Allows for the service managers to set whether this service is currently running
     *
     * @param currentlyRunning whether the service is currently running
     */
    public void setCurrentlyRunning(boolean currentlyRunning) {
        mIsCurrentlyRunning = currentlyRunning;
    }

    /**
     * @return the name of the order
     */
    public String getServiceName() {
        return fService.getClass().getSimpleName();
    }

    /**
     * Starts the scheduler and starts the service
     */
    @Override
    public synchronized void startUp() throws Exception {
        Thread.currentThread().setName(getServiceName());

        synchronized (mServiceState) {
            mServiceState = ServiceState.STARTING;
        }

        if (scheduler != null) {
            scheduler.start();
        }

        fService.startUp();
    }

    /**
     * Calls runOneIteration on the service and waits for it to complete before moving on
     */
    @Override
    public synchronized void runOneIteration() throws Exception {
        Thread.currentThread().setName(getServiceName());

        mIsCurrentlyRunning = true;

        if (scheduler != null) {
            scheduler.run();
        }

        synchronized (mServiceState) {
            mServiceState = ServiceState.RUNNING;
        }

        try {
            fService.runOneIteration();
        } catch (Exception e) {
            throw e;
        } finally {
            mIsCurrentlyRunning = false;
        }
    }

    /**
     * Shuts down the service
     */
    @Override
    public synchronized void shutDown() throws Exception {
        Thread.currentThread().setName(getServiceName());

        synchronized (mServiceState) {
            mServiceState = ServiceState.STOPPING;
        }

        fService.shutDown();

        synchronized (mServiceState) {
            mServiceState = ServiceState.STOPPED;
        }
    }
}
