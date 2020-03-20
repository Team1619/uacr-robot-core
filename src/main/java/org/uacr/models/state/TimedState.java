package org.uacr.models.state;

import org.uacr.robot.ModelFactory;
import org.uacr.utilities.Config;
import org.uacr.utilities.Timer;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.Set;

/**
 * A shell that runs a state until it is done or it times out
 * I'm not sure this is useful in the current version of the framework
 */

public class TimedState implements State {

    private static final Logger sLogger = LogManager.getLogger(TimedState.class);

    private final String fStateName;
    private final State fSubState;
    private final String fSubStateName;
    private final Timer fTimer = new Timer();
    private final int fTimeout;

    public TimedState(ModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
        fStateName = name;

        fSubStateName = config.getString("state");
        fSubState = modelFactory.createState(fSubStateName, parser, parser.getConfig(fStateName));
        fTimeout = config.getInt("timeout");
    }

    @Override
    public Set<State> getSubStates() {
        return Set.of(fSubState);
    }

    @Override
    public void initialize() {
        sLogger.debug("Entering Timed State {}", fStateName);
        fTimer.start(fTimeout);
    }

    @Override
    public void update() {
    }

    @Override
    public void dispose() {
        sLogger.trace("Leaving Timed State {}", fStateName);
        fTimer.reset();
    }

    @Override
    public boolean isDone() {
        return fSubState.isDone() || fTimer.isDone();
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
