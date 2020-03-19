package org.uacr.utilities.services.managers;

import org.uacr.utilities.services.Service;
import org.uacr.utilities.services.ServiceState;
import org.uacr.utilities.services.ServiceWrapper;

import java.util.List;

/**
 * Runs the services in independent threads using a scheduler
 */

public class AsyncServiceManager extends ServiceManager {

	public AsyncServiceManager(Service... services) {
		this(List.of(services));
	}

	public AsyncServiceManager(List<Service> services) {
		super(services);

		setCurrentState(ServiceState.AWAITING_START);
	}

	@Override
	protected void onError(ServiceWrapper service, Exception exception) {
		RuntimeException e = new RuntimeException(service.getServiceName() + " has failed in a " + service.getServiceState() + " state", exception);

		e.setStackTrace(new StackTraceElement[]{});

		e.printStackTrace();

		if (getCurrentState() == ServiceState.STARTING) {
			stop();
		}
	}

	// States the services
	@Override
	public void start() {
		getExecutor().submit(() -> {
			setCurrentState(ServiceState.STARTING);

			for (ServiceWrapper service : getServices()) {
				startUpService(service);
			}

			if (getCurrentState() != ServiceState.STOPPING) {
				setCurrentState(ServiceState.RUNNING);

				while (getCurrentState() == ServiceState.RUNNING) {
					update();
				}
			}

			for (ServiceWrapper service : getServices()) {
				shutDownService(service);
			}

			setCurrentState(ServiceState.STOPPED);
		});
	}

	@Override
	public void awaitHealthy() {
		while (getCurrentState().equals(ServiceState.AWAITING_START) || getCurrentState().equals(ServiceState.STARTING))
			;
	}

	@Override
	public void update() {
		long nextRuntime = Long.MAX_VALUE;

		for (ServiceWrapper service : getServices()) {
			long serviceRuntime = service.nextRunTimeMilliseconds();
			if (serviceRuntime < nextRuntime) {
				nextRuntime = serviceRuntime;
			}
		}

		try {
			Thread.sleep(((nextRuntime - System.nanoTime()) / 1000000) - 1);
			while (nextRuntime - System.nanoTime() > 0);
		} catch (Exception e) {
		}

		for (ServiceWrapper service : getServices()) {
			if (service.shouldRun()) {
				service.setCurrentlyRunning(true);
				getExecutor().submit(() -> {
					updateService(service);
				});
			}
		}
	}

	@Override
	public void stop() {
		setCurrentState(ServiceState.STOPPING);
	}

	@Override
	public void awaitStopped() {
		while (!getCurrentState().equals(ServiceState.STOPPED)) ;
	}
}
