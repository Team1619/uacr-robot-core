package org.uacr.events.sim;

import java.util.Map;

public class SimInputVectorSetEvent {

    public final Map<String, Double> fValues;
    public final String fName;

    public SimInputVectorSetEvent(String name, Map<String, Double> values) {
        fValues = values;
        fName = name;
    }
}
