package org.uacr.models.state;

import org.uacr.robot.AbstractModelFactory;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A shell that handles running multiple states simultaneously
 * Assembles a list of all the states it wants to run and passes it to the state machine
 */

public class ParallelState implements State {

    private static final Logger sLogger = LogManager.getLogger(ParallelState.class);

    private final AbstractModelFactory fModelFactory;
    private final Set<State> fForegroundStates;
    private final Set<State> fBackgroundStates;
    private final String fStateName;

    public ParallelState(AbstractModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
        fModelFactory = modelFactory;
        fForegroundStates = new HashSet<>();
        fBackgroundStates = new HashSet<>();
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
        Stream.concat(fForegroundStates.stream(), fBackgroundStates.stream())
                .map(State::getSubStates).forEach(states::addAll);
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
        return fForegroundStates.stream().allMatch(State::isDone);
    }

    @Override
    public Set<String> getSubsystems() {
        // Returns a list of all the subsystems required by all the states it is running
        return Stream.concat(fForegroundStates.stream(), fBackgroundStates.stream())
                .flatMap(state -> getSubsystems().stream()).collect(Collectors.toSet());
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
