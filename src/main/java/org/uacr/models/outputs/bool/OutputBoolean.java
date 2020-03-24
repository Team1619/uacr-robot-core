package org.uacr.models.outputs.bool;

import org.uacr.utilities.Config;

public abstract class OutputBoolean {

    protected final Object fName;
    protected final boolean fIsInverted;

    public OutputBoolean(Object name, Config config) {
        fName = name;
        fIsInverted = config.getBoolean("inverted", false);
    }

    public void initalize() {

    }

    public abstract void setHardware(boolean outputValue);

    public abstract void processFlag(String flag);
}
