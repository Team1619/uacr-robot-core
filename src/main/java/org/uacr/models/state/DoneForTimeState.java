package org.uacr.models.state;

import org.uacr.robot.ModelFactory;
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

    private static final Logger sLogger = LogManager.getLogger(DoneForTimeState.class);

    private final String fStateName;
    private final State fSubState;
    private final String fSubStateName;

    private final Timer fStateTimer = new Timer();
    private final Timer fMaxTimer = new Timer();

    private final int fStateTimeout;

    private int fMaxTimeout;

    public DoneForTimeState(ModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
        fStateName = name;

        fSubStateName = config.getString("state");
        fSubState = modelFactory.createState(fSubStateName, parser, parser.getConfig(fSubStateName));

        fStateTimeout = config.getInt("state_timeout");
        fMaxTimeout = config.getInt("max_timeout", -1);
    }

    @Override
    public Set<State> getSubStates() {
        // Returns a list of all the states it is currently running
        Set<State> states = new HashSet<>();
        states.add(fSubState);
        states.addAll(fSubState.getSubStates());
        return states;
    }

    @Override
    public void initialize() {
        sLogger.debug("Entering Done For Time State {}", fStateName);
        if (fMaxTimeout != -1) {
            fMaxTimer.start(fMaxTimeout);
        }
    }

    @Override
    public void update() {

    }

    @Override
    public void dispose() {
        sLogger.trace("Leaving Done For Time State {}", fStateName);
        fStateTimer.reset();
    }

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

    @Override
    public Set<String> getSubsystems() {
        return fSubState.getSubsystems();
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
