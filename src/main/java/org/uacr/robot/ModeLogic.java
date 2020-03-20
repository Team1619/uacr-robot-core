package org.uacr.robot;

import org.uacr.models.state.State;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.RobotConfiguration;

/**
 * Base for handling isReady and isDone for states in a particular mode
 */

public abstract class ModeLogic {

    protected InputValues fSharedInputValues;
    protected RobotConfiguration fRobotConfiguration;

    public ModeLogic(InputValues inputValues, RobotConfiguration robotConfiguration) {
        fSharedInputValues = inputValues;
        fRobotConfiguration = robotConfiguration;
    }

    public abstract void initialize();

    public abstract void update();

    public abstract void dispose();

    /**
     * Defines when each state is ready
     *
     * @param name the name of the state being checked if it's ready
     * @return whether the state is ready
     */
    public abstract boolean isReady(String name);

    /**
     * Determines when a state is done
     * This is a place to override the isDone logic in the behavior for a specific state
     *
     * @param name  the state being checked if it's done
     * @param state the state object (used to call the behavior's isDone)
     * @return Whether the state is done
     */
    public abstract boolean isDone(String name, State state);
}
