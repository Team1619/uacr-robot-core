package org.uacr.utilities.services;

import org.uacr.utilities.Lists;

import java.util.List;

/**
 * Runs a multiService (a class that handles running multiple services) using a scheduler
 */

public class ScheduledMultiService extends MultiService implements ScheduledService {

    private Scheduler fScheduler;

    public ScheduledMultiService(Scheduler scheduler, List<Service> services) {
        super(services);

        fScheduler = scheduler;
    }

    public ScheduledMultiService(Scheduler scheduler, Service... services) {
        this(scheduler, Lists.of(services));
    }

    @Override
    public Scheduler scheduler() {
        return fScheduler;
    }
}
