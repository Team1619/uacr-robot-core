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
 * Can only run one state at a time but that state can be any type of state including parallels
 */

public class SequencerState implements State {

    private static final Logger sLogger = LogManager.getLogger(SequencerState.class);

    private final AbstractModelFactory fModelFactory;
    private final List<State> fStates;
    private final String fStateName;

    private State mCurrentState;
    private int mCurrentStateIndex;

    /**
     * @param modelFactory so it can create its substates
     * @param name of the sequencer state
     * @param parser the yaml parser for the sequencer
     * @param config the config for the sequencer
     */

    public SequencerState(AbstractModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
        fModelFactory = modelFactory;
        fStates = new ArrayList<>();
        fStateName = name;

        // Reads the sequence of states from the config
        for (Object stateName : config.getList("sequence")) {
            fStates.add(fModelFactory.createState((String) stateName, parser, parser.getConfig(stateName)));
        }

        //Get the first state in the list
        mCurrentStateIndex = 0;
        mCurrentState = fStates.get(mCurrentStateIndex);
    }

    /**
     * @return a list of all the states it is currently running in this frame
     */

    @Override
    public Set<State> getSubStates() {
        Set<State> states = new HashSet<>();
        states.add(mCurrentState);
        states.addAll(mCurrentState.getSubStates());
        return states;
    }

    /**
     * Called at the beginning of the state
     */

    @Override
    public void initialize() {
        sLogger.debug("");
        sLogger.debug("Entering Sequencer State {}", fStateName);
    }

    /**
     * Checks if the sequence is finished
     * If not and the current state is done move on to the next state
     */

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

    /**
     * Get the first state in the list again so it is ready to be used next time the sequence is active
     */

    @Override
    public void dispose() {
        sLogger.trace("Leaving Sequencer State {}", fStateName);
        mCurrentStateIndex = 0;
        mCurrentState = fStates.get(mCurrentStateIndex);
    }

    /**
     * @return true if we have moved past the last state in the list
     */

    @Override
    public boolean isDone() {
        return mCurrentStateIndex >= fStates.size();
    }

    /**
     * @return a list of all the subsystems required by all the states that will be run sometime during the sequence
     */

    @Override
    public Set<String> getSubsystems() {
        Set<String> subsystems = new HashSet<>();
        for (State state : fStates) {
            subsystems.addAll(state.getSubsystems());
        }
        return subsystems;
    }

    /**
     * @return the name of the sequencer state
     */

    @Override
    public String getName() {
        return fStateName;
    }

    /**
     * This method is used so the .toString will return the name of the state if called on this object
     * @return the name of the sequencer state
     */

    @Override
    public String toString() {
        return getName();
    }
}
