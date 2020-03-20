package org.uacr.shared.concretions;

import org.uacr.shared.abstractions.EventBus;
import org.uacr.utilities.eventbus.AsyncEventBus;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.concurrent.Executors;

@Singleton
public class SharedEventBus implements EventBus {

    private static final Logger sLogger = LogManager.getLogger(SharedEventBus.class);

    private org.uacr.utilities.eventbus.EventBus fEventBus;

    public SharedEventBus() {
        fEventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));
    }

    @Override
    public void register(Object object) {
        sLogger.trace("Registering object '{}'", object);

        fEventBus.register(object);
    }

    @Override
    public void post(Object object) {
        sLogger.trace("Posting object '{}'", object);

        fEventBus.post(object);
    }

    @Override
    public void unregister(Object object) {
        sLogger.debug("Unregistering object '{}'", object);

        fEventBus.unregister(object);
    }
}
