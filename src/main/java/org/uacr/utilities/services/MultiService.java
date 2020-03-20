package org.uacr.utilities.services;

import java.util.Arrays;
import java.util.List;

public class MultiService implements Service {

    private List<Service> fServices;

    public MultiService(Service... services) {
        fServices = Arrays.asList(services);
    }

    @Override
    public void startUp() throws Exception {
        for (Service service : fServices) {
            Thread.currentThread().setName(service.getClass().getSimpleName());
            service.startUp();
        }
    }

    @Override
    public void runOneIteration() throws Exception {
        for (Service service : fServices) {
            Thread.currentThread().setName(service.getClass().getSimpleName());
            service.runOneIteration();
        }
    }

    @Override
    public void shutDown() throws Exception {
        for (Service service : fServices) {
            Thread.currentThread().setName(service.getClass().getSimpleName());
            service.shutDown();
        }
    }
}
