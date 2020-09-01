package org.uacr.events.sim;

import java.util.Map;

/**
 * Eventbus event for setting an InputVector in sim mode
 */

public class SimInputVectorSetEvent {

    public final Map<String, Double> fValues;
    public final String fName;

    /**
     * @param name the name of the InputVector
     * @param values the new values for the InputVector
     */
    public SimInputVectorSetEvent(String name, Map<String, Double> values) {
        fValues = values;
        fName = name;
    }
}
