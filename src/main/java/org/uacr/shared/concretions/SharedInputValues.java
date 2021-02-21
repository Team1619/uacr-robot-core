package org.uacr.shared.concretions;

import org.uacr.shared.abstractions.InputValues;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores the values from all of the inputs
 * Shared between most classes so values can be accessed and assigned
 */

@Singleton
public class SharedInputValues implements InputValues {

    private static final Logger sLogger = LogManager.getLogger(SharedInputValues.class);

    private final Map<String, Boolean> fInputBooleans;
    private final Map<String, Boolean> fInputBooleanRisingEdges;
    private final Map<String, Boolean> fInputBooleanFallingEdges;
    private final Map<String, Double> fInputNumerics;
    private final Map<String, Map<String, Double>> fInputVectors;
    private final Map<String, String> fInputStrings;
    private final Map<String, Set<String>>fInputFlags;

    /**
     * Creates maps to store the values of each input by input type and value type
     */
    public SharedInputValues() {
        fInputBooleans = new ConcurrentHashMap<>();
        fInputBooleanRisingEdges = new ConcurrentHashMap<>();
        fInputBooleanFallingEdges = new ConcurrentHashMap<>();
        fInputNumerics = new ConcurrentHashMap<>();
        fInputVectors = new ConcurrentHashMap<>();
        fInputStrings = new ConcurrentHashMap<>();
        fInputFlags = new ConcurrentHashMap<>();
    }

    /**
     * @param name of the InputBoolean being read (an input that returns true or false such as a button)
     * @return the value of the InputBoolean, return false if the InputBoolean requested does not exist
     */

    @Override
    public boolean getBoolean(String name) {
        return fInputBooleans.getOrDefault(name, false);
    }

    /**
     * @param name of the InputBoolean being read (an input that returns true or false such as a button)
     * @return true on the frame that the InputBoolean becomes true, return false all other frames
     */

    @Override
    public boolean getBooleanRisingEdge(String name) {
        boolean risingEdge = fInputBooleanRisingEdges.getOrDefault(name, false);
        // Rising edge will remain for a frame
        // Comment in to use single use rising edge
        //fBooleanRisingEdgeInputs.put(name, false);
        return risingEdge;
    }

    /**
     * @param name of the InputBoolean being read (an input that returns true or false such as a button)
     * @return true on the frame that the InputBoolean becomes false, return false all other frames
     */

    @Override
    public boolean getBooleanFallingEdge(String name) {
        boolean fallingEdge = fInputBooleanFallingEdges.getOrDefault(name, false);
        // Falling edge will remain for a frame
        // Comment in to use single use falling edge
        //fBooleanFallingEdgeInputs.put(name, false);
        return fallingEdge;
    }

    /**
     * @return the map containing the current values of all the InputBooleans
     */

    @Override
    public Map<String, Boolean> getAllBooleans() {
        return fInputBooleans;
    }

    /**
     * @param name of the InputNumeric being read (an input that returns a numerical value such as a joystick)
     * @return the value of the InputNumeric, return 0.0 if the InputNumeric requested does not exist
     */
    @Override
    public double getNumeric(String name) {
        return fInputNumerics.getOrDefault(name, 0.0);
    }

    /**
     * @return a map containing the current values of all the InputNumerics
     */

    @Override
    public Map<String, Double> getAllNumerics() {
        return fInputNumerics;
    }

    /**
     * @param name of the InputVector being read (an input that returns a set of values such as a camera)
     * @return a map of the values of the InputVector, return an empty map if the InputVector requested does not exist
     */

    @Override
    public Map<String, Double> getVector(String name) {
        return fInputVectors.getOrDefault(name, new HashMap<>());
    }

    /**
     * Input flags are used to pass extra information into inputs, they exist until they are read once
     * @param name of the input the flag is to be set on
     * @param flag a string with the desired information
     */

    @Override
    public void setInputFlag(String name, String flag) {

        if (!fInputFlags.containsKey(name)) {
            fInputFlags.put(name, new HashSet<String>());
        }
        fInputFlags.get(name).add(flag);

    }

    /**
     * Input flags are used to pass extra information into inputs, they exist until they are read once
     * @param name of the input the desired flag is attached to
     * @return the flag after removing it from the input flags map
     */

    @Override
    public Set<String> getInputFlags(String name) {
        Set<String> flags = fInputFlags.getOrDefault(name, Collections.emptySet());
        fInputFlags.remove(name);
        return flags;
    }

    /**
     * @return a map containing maps of the current values of all the InputVectors
     */

    @Override
    public Map<String, Map<String, Double>> getAllVectors() {
        return fInputVectors;
    }

    /**
     * @param name of the input string (an input that returns a string value, often an internal input such as the mode the robot is currently in)
     * @return the value attached to that string, return "DoesNotExist" if the name of the string does not exist in the map
     */

    @Override
    public String getString(String name) {
        return fInputStrings.getOrDefault(name, "DoesNotExist");
    }

    /**
     * @return a map containing the values of all the input strings
     */

    @Override
    public Map<String, String> getAllStrings() {
        return fInputStrings;
    }

    /**
     * Sets the value of an InputBoolean (an input that returns either true or false such as a button)
     * @param name of the InputBoolean to be set
     * @param value the InputBoolean should be set to
     * @return the updated value of the InputBoolean
     */

    public boolean setBoolean(String name, boolean value) {
        fInputBooleans.put(name, value);
        return value;
    }

    /**
     * Sets the rising edge of an InputBoolean (a value that is true on the frame an InputBoolean becomes true and false on all other frames)
     * @param name of the InputBoolean the rising edge is to be set on
     * @param value of the rising edge
     */

    public void setBooleanRisingEdge(String name, boolean value) {
        fInputBooleanRisingEdges.put(name, value);
    }

    /**
     * Sets the falling edge of an InputBoolean (a value that is true on the frame an InputBoolean becomes false and false on all other frames)
     * @param name of the InputBoolean the falling edge is to be set on
     * @param value of the falling edge
     */

    @Override
    public void setBooleanFallingEdge(String name, boolean value) {
        fInputBooleanFallingEdges.put(name, value);
    }

    /**
     * Sets the value of an InputNumeric (an input that returns a numeric values such as a joystick)
     * @param name of the InputNumeric to be set
     * @param value the InputNumeric should be set to
     */

    @Override
    public void setNumeric(String name, double value) {
        fInputNumerics.put(name, value);
    }

    /**
     * Sets the values of an InputVector (an input that returns a map of values such as a camera)
     * @param name of the InputVector to be set
     * @param values the InputVector should be set to
     */

    @Override
    public void setVector(String name, Map<String, Double> values) {
        fInputVectors.put(name, values);
    }

    /**
     * Sets the value of an InputString (an input that returns a string, such as a what mode the robot is in)
     * @param name of the InputString to be set
     * @param value the InputString should be set to
     */

    @Override
    public void setString(String name, String value) {
        fInputStrings.put(name, value);
    }
}
