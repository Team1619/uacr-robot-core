package org.uacr.utilities.services.managers;

import org.uacr.utilities.services.Service;
import org.uacr.utilities.services.ServiceState;
import org.uacr.utilities.services.ServiceWrapper;

import java.util.List;

public abstract class NonlinearServiceManager extends ServiceManager {

    public NonlinearServiceManager(List<Service> services) {
        super(services);

        setCurrentState(ServiceState.AWAITING_START);
    }

    protected abstract void requestServiceUpdate(ServiceWrapper service);

    @Override
    protected void onError(ServiceWrapper service, Exception exception) {
        RuntimeException e = new RuntimeException(service.getServiceName() + " has failed in a " + service.getServiceState() + " state", exception);

        e.setStackTrace(new StackTraceElement[]{});

        e.printStackTrace();

        if (getCurrentState() == ServiceState.STARTING) {
            stop();
        }
    }

    @Override
    public void start() {
        getExecutor().submit(() -> {
            setCurrentState(ServiceState.STARTING);

            for (ServiceWrapper service : getServices()) {
                startUpService(service);
            }

            getHealthyLatch().countDown();

            if (getCurrentState() != ServiceState.STOPPING) {
                Thread.currentThread().setName("NonlinearServiceManager Dispatch");

                setCurrentState(ServiceState.RUNNING);

                while (getCurrentState() == ServiceState.RUNNING) {
                    update();
                }
            }

            for (ServiceWrapper service : getServices()) {
                shutDownService(service);
            }

            getShutDownLatch().countDown();

            setCurrentState(ServiceState.STOPPED);
        });
    }

    @Override
    public void update() {
        waitUntilNextRun();

        for (ServiceWrapper service : getServices()) {
            if (service.shouldRun()) {
                service.setCurrentlyRunning(true);
                requestServiceUpdate(service);
            }
        }
    }

    public void waitUntilNextRun() {
        // Determines the next time a service should run,
        // so that the thread can sleep until that time and reduce CPU usage
        long nextRuntime = Long.MAX_VALUE;

        for (ServiceWrapper service : getServices()) {
            long serviceRuntime = service.nextRunTimeNanos();
            if (serviceRuntime < nextRuntime) {
                nextRuntime = serviceRuntime;
            }
        }

        // Sleep until the next service should run
        try {
            Thread.sleep((nextRuntime - System.nanoTime()) / 1000000);
        } catch (Exception e) {
        }
    }
}
