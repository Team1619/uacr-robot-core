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

/**
 * Handles the creation of states
 */

public abstract class ModelFactory {
	private static final Logger sLogger = LogManager.getLogger(ModelFactory.class);

	protected final InputValues fSharedInputValues;
	protected final OutputValues fSharedOutputValues;
	protected final RobotConfiguration fRobotConfiguration;
	protected final ObjectsDirectory fSharedObjectDirectory;

	@Inject
	public ModelFactory(InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fRobotConfiguration = robotConfiguration;
		fSharedObjectDirectory = objectsDirectory;
	}

	public InputBoolean createInputBoolean(Object name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public InputNumeric createInputNumeric(Object name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public InputVector createInputVector(Object name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public Behavior createBehavior(String name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public State createState(String name, YamlConfigParser parser, Config config) {
		sLogger.trace("Creating state '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		//todo - is there a better way to handle nullable

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

	public OutputNumeric createOutputNumeric(Object name, Config config, YamlConfigParser parser) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public OutputBoolean createOutputBoolean(Object name, Config config, YamlConfigParser parser) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}
}
