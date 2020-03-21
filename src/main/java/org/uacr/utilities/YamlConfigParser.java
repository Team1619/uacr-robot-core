package org.uacr.utilities;


import org.uacr.models.exceptions.ConfigurationException;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YamlConfigParser {

    private static final Logger sLogger = LogManager.getLogger(YamlConfigParser.class);

    private final Yaml fYaml = new Yaml();

    private Map<String, Map<String, Object>> fData = new HashMap<>();
    private Map<String, String> fNameTypes = new HashMap<>();

    private String fRobotVariation = "none";

    public void loadWithFolderName(String path) {

        YamlConfigParser parser = new YamlConfigParser();
        parser.load("general.yaml");
        Config config = parser.getConfig("robot");

        fRobotVariation = config.getString("robot_variation", "");

        load(path);
    }

    public void load(String path) {

        sLogger.trace("Loading config file '{}'", path);

        try {
            fData = fYaml.load(getClassLoader().getResourceAsStream(path));
        } catch (Throwable t) {
            sLogger.error(t.getMessage());
        }

        if (fData == null) {
            fData = new HashMap<>();
        }

        sLogger.trace("Loaded config file '{}'", path);

        fNameTypes = new HashMap<>();

        for (Map.Entry<String, Map<String, Object>> entry : fData.entrySet()) {
            if (entry.getValue() != null) {
                for (String name : entry.getValue().keySet()) {
                    if (!entry.getKey().equals("variations")) {
                        fNameTypes.put(name, entry.getKey());
                    }
                }
            }
        }

        if (!fRobotVariation.equals("") && fData.containsKey("variations")) {
            Map<String, Object> variation = (Map<String, Object>) fData.get("variations").get(fRobotVariation);
            if (variation != null) {
                loadVariation(variation, new ArrayList<>());
            }
        }

        fData.remove("variations");
    }

    private void loadVariation(Map<String, Object> variation, ArrayList<String> stack) {
        for (Map.Entry<String, Object> entry : variation.entrySet()) {
            if (entry.getValue() instanceof Map) {
                stack.add(entry.getKey());
                loadVariation((Map<String, Object>) entry.getValue(), stack);
                stack.remove(entry.getKey());
            } else {
                stack.add(entry.getKey());
                editMapValueWithStack(fData, (ArrayList<String>) stack.clone(), entry.getValue());
                stack.remove(entry.getKey());
            }
        }
    }

    private void editMapValueWithStack(Map data, ArrayList<String> stack, Object value) {
        if (stack.size() <= 1) {
            data.put(stack.get(0), value);
            return;
        }

        String key = stack.get(0);
        stack.remove(0);

        editMapValueWithStack((Map) data.get(key), stack, value);
    }

    public Config getConfig(Object object) {
        String name = object.toString().toLowerCase();

        sLogger.trace("Getting config with name '{}'", name);

        if (!(fNameTypes.containsKey(name) && fData.containsKey(fNameTypes.get(name)))) {
            throw new ConfigurationException("***** No config exists for name '" + name + "' *****");
        }

        String type = fNameTypes.get(name);
        try {
            return new Config(type, (Map) fData.get(type).get(name));
        } catch (ClassCastException ex) {
            throw new ConfigurationException("***** Expected map but found " + fData.getClass().getSimpleName() + "*****");
        }
    }

    public Map getData() {
        return fData;
    }

    protected ClassLoader getClassLoader() {
        return YamlConfigParser.class.getClassLoader();
    }
}
