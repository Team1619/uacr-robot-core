package org.uacr.events.sim;

import java.util.Map;

public class SimInputVectorSetEvent {

    public final String fName;
    public final Map<String, Double> fValues;

    public SimInputVectorSetEvent(String name, Map<String, Double> values) {
        this.fName = name;
        this.fValues = values;
    }
}
