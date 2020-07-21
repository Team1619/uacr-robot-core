package org.uacr.shared.abstractions;

/**
 * Stores the values to be sent to all of the outputs
 */

import java.util.Map;

public interface OutputValues {

    // All
    Map<String, Object> getAllOutputs();

    void setOutputFlag(String name, String flag);

    String getOutputFlag(String name);

    // Output Numeric
    void setNumeric(String outputNumericName, String outputType, double outputValue);

    void setNumeric(String outputNumericName, String outputType, double outputValue, String flag);

    Map<String, Object> getOutputNumericValue(String outputNumericName);


    // Output Boolean
    void setBoolean(String outputBooleanName, boolean outputValue);

    boolean getBoolean(String outputBooleanName);
}
