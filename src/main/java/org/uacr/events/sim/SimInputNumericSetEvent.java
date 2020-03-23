package org.uacr.events.sim;

public class SimInputNumericSetEvent {

    public final double fValue;
    public final String fName;

    public SimInputNumericSetEvent(String name, double value) {
        fValue = value;
        fName = name;
    }
}
