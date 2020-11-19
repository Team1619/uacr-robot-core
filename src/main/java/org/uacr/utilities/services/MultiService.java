package org.uacr.utilities.services;

import java.util.Arrays;
import java.util.List;

/**
 * Runs multiple services at one time
 */

public class MultiService implements Service {

    private final List<Service> fServices;

    public MultiService(Service... services) {
        fServices = Arrays.asList(services);
    }

    /**
     * Starts up all services handled by this multiService in a single thread
     */
    @Override
    public void startUp() throws Exception {
        for (Service service : fServices) {
            Thread.currentThread().setName(service.getClass().getSimpleName());
            service.startUp();
        }
    }

    /**
     * Calls runOneIteration all services handled by this multiService in a single thread
     */
    @Override
    public void runOneIteration() throws Exception {
        for (Service service : fServices) {
            Thread.currentThread().setName(service.getClass().getSimpleName());
            service.runOneIteration();
        }
    }

    /**
     * Shuts down all services handled by this multiService in a single thread
     */
    @Override
    public void shutDown() throws Exception {
        for (Service service : fServices) {
            Thread.currentThread().setName(service.getClass().getSimpleName());
            service.shutDown();
        }
    }
}
