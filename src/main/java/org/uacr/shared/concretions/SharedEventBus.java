package org.uacr.shared.concretions;

import org.uacr.shared.abstractions.EventBus;
import org.uacr.utilities.eventbus.AbstractEventBus;
import org.uacr.utilities.eventbus.AsyncEventBus;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.concurrent.Executors;

/**
 * Handles the distribution of messages in Sim mode
 * Objects register with the event bus and then will receive messages posted
 */

@Singleton
public class SharedEventBus implements EventBus {

    private static final Logger sLogger = LogManager.getLogger(SharedEventBus.class);

    private final AbstractEventBus fEventBus;

    /**
     * Create a new AsyncEventBus (can handle multiple messages at the same time)
     */
    public SharedEventBus() {
        fEventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));
    }

    /**
     * Allows an object to register to receive events
     * @param object the object registering
     */
    @Override
    public void register(Object object) {
        sLogger.trace("Registering object '{}'", object);

        fEventBus.register(object);
    }

    /**
     * Posts an event to the EventBus
     * @param object the event to be posted
     */
    @Override
    public void post(Object object) {
        sLogger.trace("Posting object '{}'", object);

        fEventBus.post(object);
    }

    /**
     * Allows an object to unregister to quit receiving events
     * @param object the object unregistering
     */

    @Override
    public void unregister(Object object) {
        sLogger.debug("Unregistering object '{}'", object);

        fEventBus.unregister(object);
    }
}
