package org.uacr.models.state;

import org.uacr.robot.AbstractModelFactory;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A shell that handles running a sequence of states
 * Passes the current state it wants to run to the state machine
 */

public class SequencerState implements State {

    private static final Logger sLogger = LogManager.getLogger(SequencerState.class);

    private final AbstractModelFactory fModelFactory;
    private final List<State> fStates;
    private final String fStateName;

    private State mCurrentState;
    private int mCurrentStateIndex;

    public SequencerState(AbstractModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
        fModelFactory = modelFactory;
        fStates = new ArrayList<>();
        fStateName = name;

        for (Object stateName : config.getList("sequence")) {
            fStates.add(fModelFactory.createState((String) stateName, parser, parser.getConfig(stateName)));
        }

        mCurrentStateIndex = 0;
        mCurrentState = fStates.get(mCurrentStateIndex);
    }

    @Override
    public Set<State> getSubStates() {
        // returns a list of all the states it is currently running in this frame
        Set<State> states = new HashSet<>();
        states.add(mCurrentState);
        states.addAll(mCurrentState.getSubStates());
        return states;
    }

    @Override
    public void initialize() {
        sLogger.debug("");
        sLogger.debug("Entering Sequencer State {}", fStateName);
    }

    @Override
    public void update() {

        if (mCurrentStateIndex >= fStates.size()) {
            return;
        }
        // Increments through the sequence
        if (mCurrentState.isDone()) {
            if (mCurrentStateIndex < (fStates.size())) {
                mCurrentStateIndex++;
                if (mCurrentStateIndex < (fStates.size())) {
                    mCurrentState = fStates.get(mCurrentStateIndex);
                }
            }
        }
    }

    @Override
    public void dispose() {
        sLogger.trace("Leaving Sequencer State {}", fStateName);
        mCurrentStateIndex = 0;
        mCurrentState = fStates.get(mCurrentStateIndex);
    }

    @Override
    public boolean isDone() {
        return mCurrentStateIndex >= fStates.size();
    }

    @Override
    public Set<String> getSubsystems() {
        // Returns a list of all the subsystems required by all the states that will be run sometime during the sequence
        Set<String> subsystems = new HashSet<>();
        for (State state : fStates) {
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
