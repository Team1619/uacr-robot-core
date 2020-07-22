package org.uacr.models.state;

import org.uacr.robot.AbstractModelFactory;
import org.uacr.utilities.Config;
import org.uacr.utilities.Timer;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * A shell that runs a state until it is done plus a certain amount of time has passed or it times out
 */

public class DoneForTimeState implements State {

    /**
     * Shell class for all DoneForTimeStates
     * DoneForTimeStates wrap a single state, parallel or sequence and pass it to the state machine to be run using two parameters
     * a state timeout which causes the robot to stay in the state for the specified amount of time after the state is done
     * and a max timeout which causes the robot to say the state is done the specified amount of time after the state begins regardless of the status of the state
     */

    private static final Logger sLogger = LogManager.getLogger(DoneForTimeState.class);

    private final Timer fStateTimer;
    private final Timer fMaxTimer;
    private final State fSubState;
    private final int fStateTimeout;
    private final int fMaxTimeout;
    private final String fStateName;
    private final String fSubStateName;

    /**
     * @param modelFactory so the doneForTimeState can create its substate
     * @param name the name of the DoneForTimeState
     * @param parser the ymal parser for the DoneForTimeState
     * @param config the config of the DoneForTimeState
     */

    public DoneForTimeState(AbstractModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
        fStateTimer = new Timer();
        fMaxTimer = new Timer();
        fStateTimeout = config.getInt("state_timeout");
        fMaxTimeout = config.getInt("max_timeout", -1);
        fStateName = name;
        fSubStateName = config.getString("state");

        fSubState = modelFactory.createState(fSubStateName, parser, parser.getConfig(fSubStateName));
    }

    /**
     * @return a list of the child state and all the states contained within the substate
     */

    @Override
    public Set<State> getSubStates() {
        // Returns a list of all the states it is currently running
        Set<State> states = new HashSet<>();
        states.add(fSubState);
        states.addAll(fSubState.getSubStates());
        return states;
    }

    /**
     * To initialize check if a max timout has been specified and start the timer if it has
     */

    @Override
    public void initialize() {
        sLogger.debug("Entering Done For Time State {}", fStateName);
        if (fMaxTimeout != -1) {
            fMaxTimer.start(fMaxTimeout);
        }
    }

    /**
     * Called every frame
     */

    @Override
    public void update() {

    }

    /**
     * Reset the state timer to clear it for the next time the state is used
     */

    @Override
    public void dispose() {
        sLogger.trace("Leaving Done For Time State {}", fStateName);
        fStateTimer.reset();
    }

    /**
     * Start a timer with the specified state timeout time when the state isDone
     * @return true if the state timeout timer or the max timout timer finishes
     */

    @Override
    public boolean isDone() {
        //Start a timer when the state is done
        if (fSubState.isDone()) {
            if (!fStateTimer.isStarted()) {
                fStateTimer.start(fStateTimeout);
            }

        } else if (fStateTimer.isStarted()) {
            fStateTimer.reset();
        }

        return fStateTimer.isDone() || fMaxTimer.isDone();
    }

    /**
     * @return a list of the subsystems required by the substate
     */

    @Override
    public Set<String> getSubsystems() {
        return fSubState.getSubsystems();
    }

    /**
     * @return the name of the DoneForTimeState
     */

    @Override
    public String getName() {
        return fStateName;
    }

    /**
     * This method is used so the .toString will return the name of the state if called on this object
     * @return the name of the DoneForTimeState
     */

    @Override
    public String toString() {
        return getName();
    }
}
