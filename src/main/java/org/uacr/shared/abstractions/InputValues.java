package org.uacr.shared.abstractions;

import java.util.Map;

public interface InputValues {

    boolean getBoolean(String name);

    boolean getBooleanRisingEdge(String name);

    boolean getBooleanFallingEdge(String name);

    Map<String, Boolean> getAllBooleans();

    double getNumeric(String name);

    Map<String, Double> getAllNumerics();

    Map<String, Double> getVector(String name);

    void setInputFlag(String name, String flag);

    String getInputFlag(String name);

    Map<String, Map<String, Double>> getAllVectors();

    String getString(String name);

    Map<String, String> getAllStrings();

    boolean setBoolean(String name, boolean value);

    void setBooleanRisingEdge(String name, boolean value);

    void setBooleanFallingEdge(String name, boolean value);

    void setNumeric(String name, double value);

    void setVector(String name, Map<String, Double> values);

    void setString(String name, String value);
}
