package org.uacr.models.inputs.vector;

import org.uacr.utilities.Config;

import java.util.Map;

public abstract class InputVector {

    protected final Object fName;

    public InputVector(Object name, Config config) {
        fName = name;
    }

    public abstract void initialize();

    public abstract void update();

    public abstract Map<String, Double> get();

    public abstract void processFlag(String flag);

    public Object getName() {
        return fName;
    }
}
