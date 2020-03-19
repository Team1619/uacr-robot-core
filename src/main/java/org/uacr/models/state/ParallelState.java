package org.uacr.models.state;

import org.uacr.robot.ModelFactory;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * A shell that handles running multiple states simultaneously
 * Assembles a list of all the states it wants to run and passes it to the state machine
 */

public class ParallelState implements State {

	private static final Logger sLogger = LogManager.getLogger(ParallelState.class);

	private final ModelFactory fModelFactory;
	private final String fStateName;

	private Set<State> fForegroundStates = new HashSet<>();
	private Set<State> fBackgroundStates = new HashSet<>();

	public ParallelState(ModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
		fModelFactory = modelFactory;
		fStateName = name;

		for (Object foregroundStateName : config.getList("foreground_states")) {
			fForegroundStates.add(fModelFactory.createState((String) foregroundStateName, parser, parser.getConfig(foregroundStateName)));
		}

		if (config.contains("background_states")) {
			for (Object backgroundStateName : config.getList("background_states")) {
				fBackgroundStates.add(fModelFactory.createState((String) backgroundStateName, parser, parser.getConfig(backgroundStateName)));
			}
		}
	}

	@Override
	public Set<State> getSubStates() {
		// Returns a list of all the states it is currently running
		Set<State> states = new HashSet<>();
		states.addAll(fForegroundStates);
		states.addAll(fBackgroundStates);
		for (State foregroundState : fForegroundStates) {
			states.addAll(foregroundState.getSubStates());
		}
		for (State backgroundState : fBackgroundStates) {
			states.addAll(backgroundState.getSubStates());
		}
		return states;
	}

	@Override
	public void initialize() {
		sLogger.debug("Entering Parallel State {}", fStateName);
	}

	@Override
	public void update() {
	}

	@Override
	public void dispose() {
		sLogger.trace("Leaving Parallel State {}", fStateName);
	}

	@Override
	public boolean isDone() {
		for (State foregroundState : fForegroundStates) {
			if (!foregroundState.isDone()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Set<String> getSubsystems() {
		// Returns a list of all the subsystems required by all the states it is running
		Set<String> subsystems = new HashSet<>();
		for (State state : fForegroundStates) {
			subsystems.addAll(state.getSubsystems());
		}
		for (State state : fBackgroundStates) {
			subsystems.addAll(state.getSubsystems());
		}
		return subsystems;
	}

	@Override
	public String getName() {
		return fStateName;
	}

	@Override
	public String toString() {
		return getName();
	}
}
