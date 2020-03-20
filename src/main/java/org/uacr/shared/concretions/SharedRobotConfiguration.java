package org.uacr.shared.concretions;

import org.uacr.models.exceptions.ConfigurationException;
import org.uacr.models.exceptions.ConfigurationInvalidTypeException;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

@Singleton
public class SharedRobotConfiguration implements RobotConfiguration {

    private static final Logger sLogger = LogManager.getLogger(SharedRobotConfiguration.class);

    private final Yaml fYaml = new Yaml();

    private Map<String, Map<String, Object>> fData = new HashMap<>();

    @Override
    public void initialize() {
        sLogger.trace("Loading robot-configuration.yaml file");

        YamlConfigParser parser = new YamlConfigParser();
        parser.loadWithFolderName("robot-configuration.yaml");
        fData = parser.getData();

        sLogger.trace("Loaded");
    }

    @Override
    public Map<String, Set<String>> getStateNamesWithPriority() {
        Set<String> stateKeys = new LinkedHashSet<>();

        stateKeys.add("sequences");
        stateKeys.add("parallels");

        stateKeys.addAll(getSubsystemNames());

        Map<String, Map<String, List<String>>> yamlStateMaps = getMap("general", "states");

        Map<String, Set<String>> stateMap = new HashMap<>();

        for (String stateKey : stateKeys) {
            if (!yamlStateMaps.containsKey(stateKey)) {
                continue;
            }

            Map<String, List<String>> singleKeyStateMap = yamlStateMaps.get(stateKey);

            for (Map.Entry<String, List<String>> singlePriorityMap : singleKeyStateMap.entrySet()) {
                String priority = singlePriorityMap.getKey();
                priority = priority.replace("priority_level_", "");
                List<String> singlePriorityStateList = singlePriorityMap.getValue();

                if (stateMap.containsKey(priority)) {
                    stateMap.get(priority).addAll(singlePriorityStateList);
                } else {
                    Set<String> singlePriorityStateNames = new LinkedHashSet<>();

                    singlePriorityStateNames.addAll(singlePriorityStateList);

                    stateMap.put(priority, singlePriorityStateNames);
                }
            }
        }

        return stateMap;
    }

    @Override
    public Set<String> getStateNames() {
        Set<String> stateKeys = new LinkedHashSet<>();

        stateKeys.add("sequences");
        stateKeys.add("parallels");

        stateKeys.addAll(getSubsystemNames());

        Map<String, Map<String, List<String>>> yamlStateMaps = getMap("general", "states");

        Set<String> stateSet = new LinkedHashSet<>();

        for (String stateKey : stateKeys) {
            if (!yamlStateMaps.containsKey(stateKey)) {
                continue;
            }

            Map<String, List<String>> singleKeyStateMap = yamlStateMaps.get(stateKey);

            for (Map.Entry<String, List<String>> singlePriorityMap : singleKeyStateMap.entrySet()) {
                List<String> singlePriorityStateList = singlePriorityMap.getValue();

                stateSet.addAll(singlePriorityStateList);
            }
        }

        return stateSet;
    }

    @Override
    public Set<String> getSubsystemNames() {
        return getSet("general", "subsystems");
    }

    @Override
    public Set<String> getInputBooleanNames() {
        return getSet("general", "input_booleans");
    }

    @Override
    public Set<String> getInputNumericNames() {
        return getSet("general", "input_numerics");
    }

    @Override
    public Set<String> getInputVectorNames() {
        return getSet("general", "input_vectors");
    }

    @Override
    public Set<String> getOutputNumericNames() {
        return getSet("general", "output_numerics");
    }

    @Override
    public Set<String> getOutputBooleanNames() {
        return getSet("general", "output_booleans");
    }

    @Override
    public Object get(String category, String key) {
        ensureExists(category, key);
        return fData.get(category).get(key);
    }

    @Override
    public Map<String, Object> getCategory(String category) {
        ensureCategoryExists(category);
        return fData.get(category);
    }

    @Override
    public int getInt(String category, String key) {
        ensureExists(category, key);
        try {
            return (int) fData.get(category).get(key);
        } catch (Exception ex) {
            if (fData.get(category).get(key) == null) {
                throw new ConfigurationInvalidTypeException("int", key, "null");
            } else {
                throw new ConfigurationInvalidTypeException("int", key, fData.get(category).get(key));
            }
        }
    }

    @Override
    public double getDouble(String category, String key) {
        ensureExists(category, key);
        try {
            return (double) fData.get(category).get(key);
        } catch (Exception ex) {
            if (fData.get(category).get(key) == null) {
                throw new ConfigurationInvalidTypeException("double", key, "null");
            } else {
                throw new ConfigurationInvalidTypeException("double", key, fData.get(category).get(key));
            }
        }
    }

    @Override
    public boolean getBoolean(String category, String key) {
        ensureExists(category, key);
        try {
            return (boolean) fData.get(category).get(key);
        } catch (Exception ex) {
            if (fData.get(category).get(key) == null) {
                throw new ConfigurationInvalidTypeException("Boolean", key, "null");
            } else {
                throw new ConfigurationInvalidTypeException("Boolean", key, fData.get(category).get(key));
            }
        }
    }

    @Override
    public String getString(String category, String key) {
        ensureExists(category, key);
        try {
            return (String) fData.get(category).get(key);
        } catch (Exception ex) {
            if (fData.get(category).get(key) == null) {
                throw new ConfigurationInvalidTypeException("String", key, "null");
            } else {
                throw new ConfigurationInvalidTypeException("String", key, fData.get(category).get(key));
            }
        }
    }

    @Override
    public List getList(String category, String key) {
        ensureExists(category, key);
        try {
            return (List) fData.get(category).get(key);
        } catch (Exception ex) {
            throw new ConfigurationInvalidTypeException("int", key, fData.get(key));
        }
    }

    @Override
    public Map getMap(String category, String key) {
        ensureExists(category, key);
        try {
            return (Map) fData.get(category).get(key);
        } catch (Exception ex) {
            throw new ConfigurationInvalidTypeException("int", key, fData.get(key));
        }
    }

    @Override
    public Set getSet(String category, String key) {
        ensureExists(category, key);
        try {
            return new HashSet((List) fData.get(category).get(key));
        } catch (ClassCastException ex) {
            throw new ConfigurationInvalidTypeException("set", key, fData.get(category).get(key));
        }
    }

    @Override
    public <T extends Enum<T>> T getEnum(String category, String key, Class<T> enumClass) {
        String value = getString(category, key).toUpperCase();

        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationInvalidTypeException("enum", key, value);
        }
    }

    protected ClassLoader getClassLoader() {
        return YamlConfigParser.class.getClassLoader();
    }

    private void ensureExists(String category, String key) {
        ensureCategoryExists(category);
        if (!fData.get(category).containsKey(key)) {
            throw new ConfigurationException("***** No value found for key  '" + key + "' in category '" + category + "' *****");
        }
    }

    private void ensureCategoryExists(String category) {
        if (!fData.containsKey(category)) {
            throw new ConfigurationException("***** No category '" + category + "' found in SharedRobotConfiguration *****");
        }
    }

    public String toString() {
        return fData.toString();
    }

    public boolean contains(String category, String key) {
        return fData.get(category).containsKey(key);
    }

    public boolean categoryIsEmpty(String category) {
        return !fData.containsKey(category) || fData.get(category) == null;
    }
}
