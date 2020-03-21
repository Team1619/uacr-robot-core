package org.uacr.events.sim;

public class SimInputBooleanSetEvent {

    public final String fName;
    public final boolean fValue;

    public SimInputBooleanSetEvent(String name, boolean value) {
        fName = name;
        fValue = value;
    }
}
