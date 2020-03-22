package org.uacr.events.sim;

public class SimInputNumericSetEvent {

    public final String fName;
    public final double fValue;

    public SimInputNumericSetEvent(String name, double value) {
        fName = name;
        fValue = value;
    }
}
