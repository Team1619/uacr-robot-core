package org.uacr.shared.concretions;

import org.uacr.shared.abstractions.OutputValues;
import org.uacr.utilities.Maps;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SharedOutputValues implements OutputValues {

    private static final Logger sLogger = LogManager.getLogger(SharedOutputValues.class);

    private final Map<String, Map<String, Object>> fOutputNumerics;
    private final Map<String, Boolean> fOutputBooleans;
    private final Map<String, String> fOutputFlags;

    public SharedOutputValues() {
        fOutputNumerics = new ConcurrentHashMap<>();
        fOutputBooleans = new ConcurrentHashMap<>();
        fOutputFlags = new ConcurrentHashMap<>();
    }

    // All
    @Override
    public Map<String, Object> getAllOutputs() {
        Map<String, Object> allOutputs = new HashMap<>();

        for (HashMap.Entry<String, Map<String, Object>> outputNumeric : fOutputNumerics.entrySet()) {
            allOutputs.put(outputNumeric.getKey(), outputNumeric.getValue().get("value"));
        }
        allOutputs.putAll(fOutputBooleans);

        return allOutputs;
    }

    @Override
    public void setOutputFlag(String outputName, String flag) {
        fOutputFlags.put(outputName, flag);
    }

    @Override
    public String getOutputFlag(String outputName) {
        String flag = fOutputFlags.getOrDefault(outputName, "none");
        fOutputFlags.remove(outputName);
        return flag;
    }


    // Numeric
    @Override
    public void setNumeric(String outputNumericName, String outputType, double outputValue) {
        setNumeric(outputNumericName, outputType, outputValue, "none");
    }

    @Override
    public void setNumeric(String outputNumericName, String outputType, double outputValue, String profile) {
        fOutputNumerics.put(outputNumericName, Maps.of("type", outputType, "value", outputValue, "profile", profile));
    }

    @Override
    public Map<String, Object> getOutputNumericValue(String outputNumericName) {
        return fOutputNumerics.getOrDefault(outputNumericName, Maps.of("value", 0.0, "type", "percent", "profile", "none"));
    }


    // Boolean
    @Override
    public void setBoolean(String outputBooleanName, boolean outputValue) {
        fOutputBooleans.put(outputBooleanName, outputValue);
    }

    @Override
    public boolean getBoolean(String outputBooleanName) {
        return fOutputBooleans.getOrDefault(outputBooleanName, false);
    }
}
