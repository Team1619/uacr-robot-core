package org.uacr.utilities;


import org.uacr.models.exceptions.ConfigurationException;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles reading in and distributing the information in the config files
 * Handles replacing values that are overridden in a variation
 */

public class YamlConfigParser {

    private static final Logger sLogger = LogManager.getLogger(YamlConfigParser.class);

    private final Yaml fYaml;

    private Map<String, Map<String, Object>> mData;
    private Map<String, String> mNameTypes;
    private String mRobotVariation;

    public YamlConfigParser() {
        fYaml = new Yaml();

        mData = new HashMap<>();
        mNameTypes = new HashMap<>();
        mRobotVariation = "none";
    }

    /**
     * Loads a ymal file and checks if there is a specified variation
     * @param path the location of the config file, all that usually needs to be specified is the name of the file
     */
    public void loadWithFolderName(String path) {
        YamlConfigParser parser = new YamlConfigParser();
        parser.load("general.yaml");
        Config config = parser.getConfig("robot");

        mRobotVariation = config.getString("robot_variation", "");

        load(path);
    }

    /**
     * Loads a ymal file into mData, checks if there is a variation being used and values specified for that variation, removes the variations data once it has been used
     * @param path the location of the config file, all that usually needs to be specified is the name of the file
     */
    public void load(String path) {

        sLogger.trace("Loading config file '{}'", path);

        try {
            mData = fYaml.load(getClassLoader().getResourceAsStream(path));
        } catch (Throwable t) {
            sLogger.error(t.getMessage());
        }

        if (mData == null) {
            mData = new HashMap<>();
        }

        sLogger.trace("Loaded config file '{}'", path);

        mNameTypes = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : mData.entrySet()) {
            if (entry.getValue() != null) {
                for (String name : entry.getValue().keySet()) {
                    if (!entry.getKey().equals("variations")) {
                        mNameTypes.put(name, entry.getKey());
                    }
                }
            }
        }

        if (!mRobotVariation.equals("") && mData.containsKey("variations")) {
            Map<String, Object> variation = (Map<String, Object>) mData.get("variations").get(mRobotVariation);
            if (variation != null) {
                loadVariation(variation, new ArrayList<>());
            }
        }

        mData.remove("variations");
    }

    /**
     * Finds the values listed in the variations and figures out the path to that value
     * Then calls the editMapValueWithStack method to replace the value for this key in mData with the value from the variation
     * This is a recursive function
     * @param variation what variation to use
     * @param stack an ArrayList to hold the path to a particular value so it can be retraced to find the value to replace
     */
    private void loadVariation(Map<String, Object> variation, ArrayList<String> stack) {
        for (Map.Entry<String, Object> entry : variation.entrySet()) {
            if (entry.getValue() instanceof Map) {
                stack.add(entry.getKey());
                loadVariation((Map<String, Object>) entry.getValue(), stack);
                stack.remove(entry.getKey());
            } else {
                stack.add(entry.getKey());
                editMapValueWithStack(mData, (ArrayList<String>) stack.clone(), entry.getValue());
                stack.remove(entry.getKey());
            }
        }
    }

    /**
     * Replaces a value in mData with one specified in the variation
     * This is a recursive function
     * @param data mData
     * @param stack the location of the value
     * @param value the value to change it to
     */
    private void editMapValueWithStack(Map data, ArrayList<String> stack, Object value) {
        if (stack.size() <= 1) {
            data.put(stack.get(0), value);
            return;
        }

        String key = stack.get(0);
        stack.remove(0);

        editMapValueWithStack((Map) data.get(key), stack, value);
    }

    /**
     * @param object the object that the config is wanted for
     * @return a new Config with the appropriate data from mData for that object
     */
    public Config getConfig(Object object) {
        String name = object.toString().toLowerCase();

        sLogger.trace("Getting config with name '{}'", name);

        if (!(mNameTypes.containsKey(name) && mData.containsKey(mNameTypes.get(name)))) {
            throw new ConfigurationException("***** No config exists for name '" + name + "' *****");
        }

        String type = mNameTypes.get(name);
        try {
            return new Config(type, (Map) mData.get(type).get(name));
        } catch (ClassCastException ex) {
            throw new ConfigurationException("***** Expected map but found " + mData.getClass().getSimpleName() + "*****");
        }
    }

    /**
     * @return mData
     */
    public Map getData() {
        return mData;
    }

    /**
     * @return the ClassLoader for this file
     */
    protected ClassLoader getClassLoader() {
        return YamlConfigParser.class.getClassLoader();
    }
}
