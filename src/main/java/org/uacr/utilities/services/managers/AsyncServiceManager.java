package org.uacr.utilities.services.managers;

import org.uacr.utilities.Lists;
import org.uacr.utilities.services.Service;
import org.uacr.utilities.services.ServiceState;
import org.uacr.utilities.services.ServiceWrapper;

import java.util.List;

/**
 * Runs the services in independent threads using a scheduler
 */

public class AsyncServiceManager extends NonlinearServiceManager {

    public AsyncServiceManager(Service... services) {
        this(Lists.of(services));
    }

    public AsyncServiceManager(List<Service> services) {
        super(services);
    }

    @Override
    protected void requestServiceUpdate(ServiceWrapper service) {
        getExecutor().submit(() -> updateService(service));
    }
}
