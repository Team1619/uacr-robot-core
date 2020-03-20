package org.uacr.events.sim;

import java.util.Map;

public class SimInputVectorSetEvent {

    public final String name;
    public final Map<String, Double> values;

    public SimInputVectorSetEvent(String name, Map<String, Double> values) {
        this.name = name;
        this.values = values;
    }
}
