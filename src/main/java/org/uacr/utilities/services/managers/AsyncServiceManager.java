package org.uacr.utilities.services.managers;

import org.uacr.utilities.Lists;
import org.uacr.utilities.services.Service;
import org.uacr.utilities.services.ServiceWrapper;

import java.util.List;

/**
 * Runs the services in independent threads using a scheduler
 */

public class AsyncServiceManager extends NonlinearServiceManager {

    private Object fUpdateFinishedLock;

    public AsyncServiceManager(Service... services) {
        this(Lists.of(services));
    }

    public AsyncServiceManager(List<Service> services) {
        super(services);

        fUpdateFinishedLock = new Object();
    }

    @Override
    protected void requestServiceUpdate(ServiceWrapper service) {
        getExecutor().submit(() -> {
            updateService(service);

            synchronized (fUpdateFinishedLock) {
                fUpdateFinishedLock.notifyAll();
            }
        });
    }

    /**
     * Waits for the next run time or for a service to finish an update cycle.
     */
    @Override
    public void waitUntilNextRun() {

        synchronized (fUpdateFinishedLock) {

            // Determines the next time a service should run,
            // so that the thread can sleep until that time and reduce CPU usage
            long nextRuntime = Long.MAX_VALUE;

            for (ServiceWrapper service : getServices()) {
                long serviceRuntime = service.nextRunTimeNanos();
                if (serviceRuntime < nextRuntime) {
                    nextRuntime = serviceRuntime;
                }
            }

            long waitTime = ((nextRuntime - System.nanoTime()) / 1000000) + 1;

            if(waitTime <= 0) {
                return;
            }

            // Sleep until the next service should run
            try {
                fUpdateFinishedLock.wait(waitTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        super.stop();

        synchronized (fUpdateFinishedLock) {
            fUpdateFinishedLock.notifyAll();
        }
    }
}
