package org.uacr.utilities.services;

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
