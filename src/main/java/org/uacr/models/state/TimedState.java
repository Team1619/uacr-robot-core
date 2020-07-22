package org.uacr.models.state;

import org.uacr.robot.AbstractModelFactory;
import org.uacr.utilities.Config;
import org.uacr.utilities.Sets;
import org.uacr.utilities.Timer;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.Set;

/**
 * A shell that runs a state until it is done or it times out
 * I'm not sure this is useful in the current version of the framework
 * Accepts the substate and a single parameter the timout
 * The time out is how long you want to state to run for at max
 */

public class TimedState implements State {

    private static final Logger sLogger = LogManager.getLogger(TimedState.class);

    private final Timer fTimer;
    private final State fSubState;
    private final int fTimeout;
    private final String fStateName;
    private final String fSubStateName;

    /**
     * @param modelFactory so it can create its substate
     * @param name of the TimedState
     * @param parser ymal parser for the TimedState
     * @param config for the TimedState
     */

    public TimedState(AbstractModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
        fTimer = new Timer();
        fStateName = name;
        fTimeout = config.getInt("timeout");
        fSubStateName = config.getString("state");

        fSubState = modelFactory.createState(fSubStateName, parser, parser.getConfig(fStateName));
    }

    /**
     * @return the substate
     */

    @Override
    public Set<State> getSubStates() {
        return Sets.of(fSubState);
    }

    /**
     * Starts the timout timer
     */

    @Override
    public void initialize() {
        sLogger.debug("Entering Timed State {}", fStateName);
        fTimer.start(fTimeout);
    }

    /**
     * Not used for Timed States
     */

    @Override
    public void update() {
    }

    /**
     * Resets the timout timer
     */

    @Override
    public void dispose() {
        sLogger.trace("Leaving Timed State {}", fStateName);
        fTimer.reset();
    }

    /**
     * @return true if the substate is done or the timout timer is done
     */

    @Override
    public boolean isDone() {
        return fSubState.isDone() || fTimer.isDone();
    }

    /**
     * @return the subsystems used by the substate
     */

    @Override
    public Set<String> getSubsystems() {
        return fSubState.getSubsystems();
    }

    /**
     * @return the name of the Timed State
     */

    @Override
    public String getName() {
        return fStateName;
    }

    /**
     * This method is used so the .toString will return the name of the state if called on this object
     * @return the name of the Timed State
     */
    @Override
    public String toString() {
        return getName();
    }
}
