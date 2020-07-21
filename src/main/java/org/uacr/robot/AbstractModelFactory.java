package org.uacr.robot;

import org.uacr.models.behavior.Behavior;
import org.uacr.models.exceptions.ConfigurationException;
import org.uacr.models.exceptions.ConfigurationTypeDoesNotExistException;
import org.uacr.models.inputs.bool.InputBoolean;
import org.uacr.models.inputs.numeric.InputNumeric;
import org.uacr.models.inputs.vector.InputVector;
import org.uacr.models.outputs.bool.OutputBoolean;
import org.uacr.models.outputs.numeric.OutputNumeric;
import org.uacr.models.state.*;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the creation of input, output and state objects
 */

public abstract class AbstractModelFactory {

    private static final Logger sLogger = LogManager.getLogger(AbstractModelFactory.class);

    protected final InputValues fSharedInputValues;
    protected final OutputValues fSharedOutputValues;
    protected final RobotConfiguration fRobotConfiguration;
    protected final ObjectsDirectory fSharedObjectDirectory;
    private final List<AbstractModelFactory> fModelFactories;

    /**
     * @param inputValues a map that stores all the input values
     * @param outputValues a map that stores all the output values
     * @param robotConfiguration the yaml file which specifies which object to create
     * @param objectsDirectory a map to store all the objects once created
     */

    @Inject
    public AbstractModelFactory(InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
        fSharedInputValues = inputValues;
        fSharedOutputValues = outputValues;
        fRobotConfiguration = robotConfiguration;
        fSharedObjectDirectory = objectsDirectory;
        fModelFactories = new ArrayList<>();
    }

    /**
     * Creates an Output Numeric (an output that takes a numeric value such as a motor)
     * Loops through all implementations of AbstractModelFactory and tries to create the desired object with each until it finds the correct implementation
     * This is to allow for sim and robot model factories
     * @param name of the object to create
     * @param config for the object, usually contains things like ID, weather it is inverted etc.
     * @param parser the ymal parser for the object, used to read the config
     * @return the desired output numeric object
     */

    public OutputNumeric createOutputNumeric(Object name, Config config, YamlConfigParser parser) {
        for (AbstractModelFactory modelFactory : fModelFactories) {
            try {
                return modelFactory.createOutputNumeric(name, config, parser);
            } catch (ConfigurationTypeDoesNotExistException e) {
            }
        }
        throw new ConfigurationTypeDoesNotExistException(config.getType());
    }

    /**
     * Creates an Output Boolean (an output that takes a boolean value such as a solenoid)
     * Loops through all implementations of AbstractModelFactory and tries to create the desired object with each until it finds the correct implementation
     * This is to allow for sim and robot model factories
     * @param name of the object to create
     * @param config for the object, usually contains things like ID, weather it is inverted etc.
     * @param parser the ymal parser for the object, used to read the config
     * @return the desired Output Boolean object
     */

    public OutputBoolean createOutputBoolean(Object name, Config config, YamlConfigParser parser) {
        for (AbstractModelFactory modelFactory : fModelFactories) {
            try {
                return modelFactory.createOutputBoolean(name, config, parser);
            } catch (ConfigurationTypeDoesNotExistException e) {
            }
        }
        throw new ConfigurationTypeDoesNotExistException(config.getType());
    }

    /**
     * Creates an Input Boolean (an input that returns a boolean value such as a button)
     * Loops through all implementations of AbstractModelFactory and tries to create the desired object with each until it finds the correct implementation
     * This is to allow for sim and robot model factories
     * @param name of the object to create
     * @param config for the object, usually contains things like ID, weather it is inverted etc.
     * @return the desired Input Boolean object
     */

    public InputBoolean createInputBoolean(Object name, Config config) {
        for (AbstractModelFactory modelFactory : fModelFactories) {
            try {
                return modelFactory.createInputBoolean(name, config);
            } catch (ConfigurationTypeDoesNotExistException e) {
            }
        }
        throw new ConfigurationTypeDoesNotExistException(config.getType());
    }

    /**
     * Creates an Input Numeric (an input that returns a numeric value such as a joystick)
     * Loops through all implementations of AbstractModelFactory and tries to create the desired object with each until it finds the correct implementation
     * This is to allow for sim and robot model factories
     * @param name of the object to create
     * @param config for the object, usually contains things like ID, weather it is inverted etc.
     * @return the desired Input Numeric object
     */

    public InputNumeric createInputNumeric(Object name, Config config) {
        for (AbstractModelFactory modelFactory : fModelFactories) {
            try {
                return modelFactory.createInputNumeric(name, config);
            } catch (ConfigurationTypeDoesNotExistException e) {
            }
        }
        throw new ConfigurationTypeDoesNotExistException(config.getType());
    }

    /**
     * Creates an Input Vector (an input that returns a vector value (multiple values) such as a camera)
     * Loops through all implementations of AbstractModelFactory and tries to create the desired object with each until it finds the correct implementation
     * This is to allow for sim and robot model factories
     * @param name of the object to create
     * @param config for the object, usually contains things like ID, weather it is inverted etc.
     * @return the desired Input Vector object
     */

    public InputVector createInputVector(Object name, Config config) {
        for (AbstractModelFactory modelFactory : fModelFactories) {
            try {
                return modelFactory.createInputVector(name, config);
            } catch (ConfigurationTypeDoesNotExistException e) {
            }
        }
        throw new ConfigurationTypeDoesNotExistException(config.getType());
    }

    /**
     * Creates a Behavior (the logic that executes the actions of states)
     * Loops through all implementations of AbstractModelFactory and tries to create the desired object with each until it finds the correct implementation
     * This is to allow for sim and robot model factories
     * @param name of the object to create
     * @param config for the state that created it, this might need to be removed as it does not fit the current system
     * @return the desired Behavior object
     */

    public Behavior createBehavior(String name, Config config) {
        for (AbstractModelFactory modelFactory : fModelFactories) {
            try {
                return modelFactory.createBehavior(name, config);
            } catch (ConfigurationTypeDoesNotExistException e) {
            }
        }
        throw new ConfigurationTypeDoesNotExistException(config.getType());
    }

    /**
     * Creates a State (an instance of a Behavior that preforms a specific task)
     * @param name of the object to create
     * @param parser the ymal config parser used to read the config values
     * @param config for the object, contains values used by the state such as motor speeds
     * @return the desired State object
     */

    public State createState(String name, YamlConfigParser parser, Config config) {
        sLogger.trace("Creating state '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

        //Only create one instance of each state
        State state = fSharedObjectDirectory.getStateObject(name);
        //noinspection ConstantConditions
        if (state == null) {
            switch (config.getType()) {
                case "single_state":
                    state = new SingleState(this, name, config, fSharedObjectDirectory);
                    break;
                case "parallel_state":
                    state = new ParallelState(this, name, parser, config);
                    break;
                case "sequencer_state":
                    state = new SequencerState(this, name, parser, config);
                    break;
                case "timed_state":
                    state = new TimedState(this, name, parser, config);
                    break;
                case "done_for_time_state":
                    state = new DoneForTimeState(this, name, parser, config);
                    break;
                default:
                    throw new ConfigurationException("State of name " + name + " does not exist.");
            }
            fSharedObjectDirectory.setStateObject(name, state);
        }
        return state;
    }

    /**
     * Used by the implementations of AbstractModelFactory to regerster
     * @param modelFactory
     */

    public void registerModelFactory(AbstractModelFactory modelFactory) {
        fModelFactories.add(modelFactory);
    }
}