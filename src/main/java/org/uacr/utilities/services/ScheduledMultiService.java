package org.uacr.utilities.services;

/**
 * Runs a multiService (a class that handles running multiple services) using a scheduler
 */

public class ScheduledMultiService extends MultiService implements ScheduledService {

    private Scheduler fScheduler;

    public ScheduledMultiService(Scheduler scheduler, Service... services) {
        super(services);

        fScheduler = scheduler;
    }

    @Override
    public Scheduler scheduler() {
        return fScheduler;
    }
}
