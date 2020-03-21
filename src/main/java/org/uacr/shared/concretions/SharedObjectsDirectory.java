package org.uacr.shared.concretions;

import org.uacr.models.behavior.Behavior;
import org.uacr.models.inputs.bool.InputBoolean;
import org.uacr.models.inputs.numeric.InputNumeric;
import org.uacr.models.inputs.vector.InputVector;
import org.uacr.models.outputs.bool.OutputBoolean;
import org.uacr.models.outputs.numeric.OutputNumeric;
import org.uacr.models.state.State;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the creation and use of all objects (inputs, states, outputs, behaviors, talons)
 */

@Singleton
public class SharedObjectsDirectory implements ObjectsDirectory {

    private static final Logger sLogger = LogManager.getLogger(SharedObjectsDirectory.class);

    private final AbstractModelFactory fModelFactory;
    private final RobotConfiguration fRobotConfiguration;
    private final InputValues fSharedInputValues;
    private final Map<String, InputBoolean> fInputBooleanObjects = new ConcurrentHashMap<>();
    private final Map<String, InputNumeric> fInputNumericObjects = new ConcurrentHashMap<>();
    private final Map<String, InputVector> fInputVectorObjects = new ConcurrentHashMap<>();
    private final Map<String, OutputNumeric> fOutputNumericObjects = new ConcurrentHashMap<>();
    private final Map<String, OutputBoolean> fOutputBooleanObjects = new ConcurrentHashMap<>();
    private final Map<Object, State> fStateObjects = new ConcurrentHashMap<>();
    private final Map<Object, Behavior> fBehaviorObjects = new ConcurrentHashMap<>();
    private final Map<Object, Object> fHardwareObjects = new ConcurrentHashMap<>();

    @Inject
    public SharedObjectsDirectory(AbstractModelFactory modelFactory, RobotConfiguration robotConfiguration, InputValues inputValues) {
        fModelFactory = modelFactory;
        fRobotConfiguration = robotConfiguration;
        fSharedInputValues = inputValues;
    }

    //--------------------------- Inputs ----------------------------------------//

    @Override
    public void registerAllInputs(YamlConfigParser inputBooleansParser, YamlConfigParser inputNumericsParser, YamlConfigParser inputVectorsParser) {
        for (String inputBooleanName : fRobotConfiguration.getInputBooleanNames()) {
            Config config = inputBooleansParser.getConfig(inputBooleanName);
            registerInputBoolean(inputBooleanName, config);
            sLogger.trace("Registered {} in Input Booleans", inputBooleanName);
        }

        for (String inputNumericName : fRobotConfiguration.getInputNumericNames()) {
            Config config = inputNumericsParser.getConfig(inputNumericName);
            registerInputNumeric(inputNumericName, config);
            sLogger.trace("Registered {} in Input Numerics", inputNumericName);
        }

        for (String inputVectorName : fRobotConfiguration.getInputVectorNames()) {
            Config config = inputVectorsParser.getConfig(inputVectorName);
            registerInputVector(inputVectorName, config);
            sLogger.trace("Registered {} in Input Vectors", inputVectorName);
        }

        for (InputBoolean inputBoolean : fInputBooleanObjects.values()) {
            inputBoolean.initialize();
        }
        sLogger.trace("Input Booleans initialized");

        for (InputNumeric inputNumeric : fInputNumericObjects.values()) {
            inputNumeric.initialize();
        }
        sLogger.trace("Input Numerics initialized");

        for (InputVector inputVector : fInputVectorObjects.values()) {
            inputVector.initialize();
        }
        sLogger.trace("Input Vectors initialized");

    }

    @Override
    public void registerInputBoolean(String name, Config config) {
        fInputBooleanObjects.put(name, fModelFactory.createInputBoolean(name, config));
    }

    @Override
    public void registerInputNumeric(String name, Config config) {
        fInputNumericObjects.put(name, fModelFactory.createInputNumeric(name, config));
    }

    @Override
    public void registerInputVector(String name, Config config) {
        fInputVectorObjects.put(name, fModelFactory.createInputVector(name, config));
    }

    @Override
    public InputBoolean getInputBooleanObject(String name) {
        return fInputBooleanObjects.get(name);
    }

    @Override
    public InputNumeric getInputNumericObject(String name) {
        return fInputNumericObjects.get(name);
    }

    @Override
    public InputVector getInputVectorObject(String name) {
        return fInputVectorObjects.get(name);
    }

    //--------------------------- States ----------------------------------------//
    @Override
    public void registerAllStates(YamlConfigParser statesParser) {
        for (String stateName : fRobotConfiguration.getStateNames()) {
            Config config = statesParser.getConfig(stateName);
            registerStates(stateName, statesParser, config);
        }
    }

    @Override
    public void registerStates(String name, YamlConfigParser statesParser, Config config) {
        fModelFactory.createState(name, statesParser, config);
    }

    @Override
    public State getStateObject(String name) {
        return fStateObjects.get(name);
    }

    @Override
    public void setStateObject(String name, State state) {
        fStateObjects.put(name, state);
    }

    //--------------------------- Outputs ----------------------------------------//
    @Override
    public void registerAllOutputs(YamlConfigParser outputNumericsParser, YamlConfigParser outputsBooleanParser) {
        for (String outputNumericName : fRobotConfiguration.getOutputNumericNames()) {
            Config outputNumericConfig = outputNumericsParser.getConfig(outputNumericName);
            registerOutputNumeric(outputNumericName, outputNumericConfig, outputNumericsParser);
            sLogger.trace("Registered {} in Output Numerics", outputNumericName);
        }

        for (String outputBooleanName : fRobotConfiguration.getOutputBooleanNames()) {
            Config outputBooleanConfig = outputsBooleanParser.getConfig(outputBooleanName);
            registerOutputBoolean(outputBooleanName, outputBooleanConfig, outputsBooleanParser);
            sLogger.trace("Registered {} in Output Booleans", outputBooleanName);
        }
    }

    @Override
    public void registerOutputNumeric(String name, Config config, YamlConfigParser parser) {
        fOutputNumericObjects.put(name, fModelFactory.createOutputNumeric(name, config, parser));
    }

    @Override
    public void registerOutputBoolean(String name, Config config, YamlConfigParser parser) {
        fOutputBooleanObjects.put(name, fModelFactory.createOutputBoolean(name, config, parser));
    }

    @Override
    public OutputNumeric getOutputNumericObject(String outputNumericName) {
        return fOutputNumericObjects.get(outputNumericName);
    }

    @Override
    public OutputBoolean getOutputBooleanObject(String outputBooleanName) {
        return fOutputBooleanObjects.get(outputBooleanName);
    }

    //--------------------------- Behaviors ----------------------------------------//

    @Override
    public void setBehaviorObject(String name, Behavior behavior) {
        fBehaviorObjects.put(name, behavior);
    }

    @Override
    public Behavior getBehaviorObject(String name) {
        return fBehaviorObjects.get(name);
    }


    //--------------------------- Hardware Objects ----------------------------------------//

    @Override
    public void setHardwareObject(Object identifier, Object hardwareObject) {
        fHardwareObjects.put(identifier, hardwareObject);
    }

    @Override
    @Nullable
    public Object getHardwareObject(Object identifier) {
        return fHardwareObjects.getOrDefault(identifier, null);
    }
}
