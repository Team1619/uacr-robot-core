package org.uacr.models.outputs.numeric;

import org.uacr.utilities.Config;

public abstract class OutputNumeric {

    protected final Object fName;
    protected final boolean fIsInverted;

    public OutputNumeric(Object name, Config config) {
        fName = name;
        fIsInverted = config.getBoolean("inverted", false);
    }

    public void initalize() {

    }

    public abstract void setHardware(String outputType, double outputValue, String profile);

    public abstract void processFlag(String flag);
}


