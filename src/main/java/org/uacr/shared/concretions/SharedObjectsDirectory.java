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
 * Handles the creation and use of objects (inputs, states, outputs, behaviors)
 * Does not handle hardware objects
 */

@Singleton
public class SharedObjectsDirectory implements ObjectsDirectory {

    private static final Logger sLogger = LogManager.getLogger(SharedObjectsDirectory.class);

    private final AbstractModelFactory fModelFactory;
    private final RobotConfiguration fRobotConfiguration;
    private final InputValues fSharedInputValues;
    private final Map<String, InputBoolean> fInputBooleanObjects;
    private final Map<String, InputNumeric> fInputNumericObjects;
    private final Map<String, InputVector> fInputVectorObjects;
    private final Map<String, OutputNumeric> fOutputNumericObjects;
    private final Map<String, OutputBoolean> fOutputBooleanObjects;
    private final Map<Object, State> fStateObjects;
    private final Map<Object, Behavior> fBehaviorObjects;
    private final Map<Object, Object> fHardwareObjects;

    /**
     * Creates maps to store all types of objects
     * @param modelFactory used to create objects
     * @param robotConfiguration used to get lists of input, output, and state names
     * @param inputValues currently not used
     */

    @Inject
    public SharedObjectsDirectory(AbstractModelFactory modelFactory, RobotConfiguration robotConfiguration, InputValues inputValues) {
        fModelFactory = modelFactory;
        fRobotConfiguration = robotConfiguration;
        fSharedInputValues = inputValues;
        fInputBooleanObjects = new ConcurrentHashMap<>();
        fInputNumericObjects = new ConcurrentHashMap<>();
        fInputVectorObjects = new ConcurrentHashMap<>();
        fOutputNumericObjects = new ConcurrentHashMap<>();
        fOutputBooleanObjects = new ConcurrentHashMap<>();
        fStateObjects = new ConcurrentHashMap<>();
        fBehaviorObjects = new ConcurrentHashMap<>();
        fHardwareObjects = new ConcurrentHashMap<>();
    }

    //--------------------------- Inputs ----------------------------------------//

    /**
     * Loops through all inputs and calls the appropriate method to create them and store them in the appropriate map
     * Once all inputs have been created, calls initialize on all inputs
     * @param inputBooleansParser holds the information from the InputBooleans yaml file
     * @param inputNumericsParser holds the information from the InputNumerics yaml file
     * @param inputVectorsParser holds the information from the InputVectors yaml file
     */
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

    /**
     * Uses the ModelFactory to create the desired InputBoolean and stores it in the InputBooleanObjects map
     * @param name of the input to be created
     * @param config the yaml configuration for the input
     */

    @Override
    public void registerInputBoolean(String name, Config config) {
        fInputBooleanObjects.put(name, fModelFactory.createInputBoolean(name, config));
    }

    /**
     * Uses the ModelFactory to create the desired InputNumeric and stores it in the InputBooleanObjects map
     * @param name of the input to be created
     * @param config the yaml configuration for the input
     */

    @Override
    public void registerInputNumeric(String name, Config config) {
        fInputNumericObjects.put(name, fModelFactory.createInputNumeric(name, config));
    }

    /**
     * Uses the ModelFactory to create the desired InputVector and stores it in the InputBooleanObjects map
     * @param name of the input to be created
     * @param config the yaml configuration for the input
     */

    @Override
    public void registerInputVector(String name, Config config) {
        fInputVectorObjects.put(name, fModelFactory.createInputVector(name, config));
    }

    /**
     * @param name of the InputBoolean desired
     * @return the InputBoolean object
     */

    @Override
    public InputBoolean getInputBooleanObject(String name) {
        return fInputBooleanObjects.get(name);
    }

    /**
     * @param name of the InputNumeric desired
     * @return the InputNumeric object
     */

    @Override
    public InputNumeric getInputNumericObject(String name) {
        return fInputNumericObjects.get(name);
    }

    /**
     * @param name of the InputVector desired
     * @return the InputVector object
     */

    @Override
    public InputVector getInputVectorObject(String name) {
        return fInputVectorObjects.get(name);
    }

    //--------------------------- States ----------------------------------------//

    /**
     * Loops through all states and calls the method to create them and store them
     * @param statesParser holds the information from the States yaml file
     */

    @Override
    public void registerAllStates(YamlConfigParser statesParser) {
        for (String stateName : fRobotConfiguration.getStateNames()) {
            Config config = statesParser.getConfig(stateName);
            registerStates(stateName, statesParser, config);
        }
    }

    /**
     * Uses the ModelFactory to create the desired state
     * @param name of the state to be created
     * @param statesParser holds the information from the States yaml file
     * @param config for the state
     */

    @Override
    public void registerStates(String name, YamlConfigParser statesParser, Config config) {
        fModelFactory.createState(name, statesParser, config);
    }

    /**
     * @param name of desired state
     * @return the state object
     */
    @Override
    public State getStateObject(String name) {
        return fStateObjects.get(name);
    }

    /**
     * Adds a state to the StateObjects map
     * @param name of state to be added
     * @param state the state object
     */

    @Override
    public void setStateObject(String name, State state) {
        fStateObjects.put(name, state);
    }

    //--------------------------- Outputs ----------------------------------------//

    /**
     * Loops through all outputs and calls the appropriate method to create them and store them in the appropriate map
     * @param outputNumericsParser holds the information from the OutputNumerics yaml file
     * @param outputsBooleanParser holds the information from the OutputBooleans yaml file
     */

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

    /**
     * Uses the ModelFactory to create the desired OutputNumeric and stores it in the OutputNumericObjects map
     * @param name of the output to be created
     * @param config the yaml configuration for the output
     */

    @Override
    public void registerOutputNumeric(String name, Config config, YamlConfigParser parser) {
        fOutputNumericObjects.put(name, fModelFactory.createOutputNumeric(name, config, parser));
    }

    /**
     * Uses the ModelFactory to create the desired OutputBoolean and stores it in the OutputBooleanObjects map
     * @param name of the output to be created
     * @param config the yaml configuration for the output
     */

    @Override
    public void registerOutputBoolean(String name, Config config, YamlConfigParser parser) {
        fOutputBooleanObjects.put(name, fModelFactory.createOutputBoolean(name, config, parser));
    }

    /**
     * @param outputNumericName name of the desired OutputNumeric object
     * @return the OutputNumeric object
     */

    @Override
    public OutputNumeric getOutputNumericObject(String outputNumericName) {
        return fOutputNumericObjects.get(outputNumericName);
    }

    /**
     * @param outputBooleanName name of the desired OutputBoolean object
     * @return the OutputBoolean object
     */

    @Override
    public OutputBoolean getOutputBooleanObject(String outputBooleanName) {
        return fOutputBooleanObjects.get(outputBooleanName);
    }

    //--------------------------- Behaviors ----------------------------------------//

    /**
     * Adds a behavior object to the BehaviorObjects map
     * @param name of the behavior object to be added
     * @param behavior the behavior object
     */
    @Override
    public void setBehaviorObject(String name, Behavior behavior) {
        fBehaviorObjects.put(name, behavior);
    }

    /**
     * @param name of the desired behavior object
     * @return the behavior object
     */

    @Override
    @Nullable
    public Behavior getBehaviorObject(String name) {
        return fBehaviorObjects.get(name);
    }
}
