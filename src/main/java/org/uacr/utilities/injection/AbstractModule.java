package org.uacr.utilities.injection;

import java.util.HashMap;

/**
 * Handles configuration of bindings
 */

public abstract class AbstractModule {

    private final HashMap<Class, Class> fBindings;

    public AbstractModule() {
        fBindings = new HashMap<>();
    }

    protected abstract void configure();

    public void bind(Class parent, Class child) {
        if (!parent.isAssignableFrom(child)) {
            throw new RuntimeException("Cannot bind " + child + " to " + parent);
        }

        fBindings.put(parent, child);
    }

    public HashMap<Class, Class> getBindings() {
        return (HashMap<Class, Class>) fBindings.clone();
    }
}
