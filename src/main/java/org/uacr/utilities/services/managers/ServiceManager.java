package org.uacr.utilities.services.managers;

import org.uacr.utilities.services.Service;
import org.uacr.utilities.services.ServiceState;
import org.uacr.utilities.services.ServiceWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages a group of services
 */

public abstract class ServiceManager {

    private final ExecutorService fExecutor;
    private final List<ServiceWrapper> fServices;
    private final CountDownLatch fHealthyLatch;
    private final CountDownLatch fShutDownLatch;

    private ServiceState mCurrentState;

    //Create a new thread and serviceWrappers for each service that it is managing
    public ServiceManager(List<Service> services) {
        fExecutor = Executors.newCachedThreadPool();
        fServices = Collections.synchronizedList(new ArrayList<>());

        fHealthyLatch = new CountDownLatch(1);
        fShutDownLatch = new CountDownLatch(1);

        mCurrentState = ServiceState.AWAITING_START;

        for (Service service : services) {
            fServices.add(new ServiceWrapper(service));
        }
    }

    // Gets the state of the service manager (same as the serviceStates)
    protected ServiceState getCurrentState() {
        synchronized (mCurrentState) {
            return mCurrentState;
        }
    }

    // Sets the state of the service manager (same as the serviceStates)
    protected void setCurrentState(ServiceState currentState) {
        synchronized (mCurrentState) {
            mCurrentState = currentState;
        }
    }

    // Gets a list of the services the serviceManager is managing
    protected List<ServiceWrapper> getServices() {
        synchronized (fServices) {
            return fServices;
        }
    }

    // Gets the Executor that is running the threadpool
    protected ExecutorService getExecutor() {
        synchronized (fExecutor) {
            return fExecutor;
        }
    }

    protected CountDownLatch getHealthyLatch() {
        return fHealthyLatch;
    }

    protected CountDownLatch getShutDownLatch() {
        return fShutDownLatch;
    }

    // Starts one service
    protected void startUpService(ServiceWrapper service) {
        try {
            service.startUp();
        } catch (Exception e) {
            onError(service, e);
        }
    }

    // Updates one service
    protected void updateService(ServiceWrapper service) {
        try {
            service.runOneIteration();
        } catch (Exception e) {
            onError(service, e);
        }
    }

    // Shuts done one service
    protected void shutDownService(ServiceWrapper service) {
        try {
            service.shutDown();
        } catch (Exception e) {
            onError(service, e);
        }
    }

    // Executes when there is an error
    protected abstract void onError(ServiceWrapper service, Exception exception);

    // Tells the executor to use an open thread to call start up
    public abstract void start();

    // Waits until every service is running
    public void awaitHealthy() {
        try {
            getHealthyLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Tells the executor to use an open thread to call runUpdate
    public abstract void update();

    // Sets the state to stopping
    public void stop() {
        setCurrentState(ServiceState.STOPPING);
    }

    // Waits until the services are stopped
    public void awaitStopped() {
        try {
            getShutDownLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
