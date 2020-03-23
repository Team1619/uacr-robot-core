package org.uacr.events.sim;

public class SimInputBooleanSetEvent {

    public final boolean fValue;
    public final String fName;

    public SimInputBooleanSetEvent(String name, boolean value) {
        fValue = value;
        fName = name;
    }
}
