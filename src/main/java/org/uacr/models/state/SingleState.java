package org.uacr.models.state;

import org.uacr.models.behavior.Behavior;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.utilities.Config;
import org.uacr.utilities.Sets;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;

/**
 * The base for all states
 * States preform a single action on a single subsystem
 * One implementation is created for each state listed under 'single_state' in the state yaml file
 * Creates one copy of the associated behavior and points all states that use it to the same instance
 */

public class SingleState implements State {

    private static final Logger sLogger = LogManager.getLogger(SingleState.class);

    private final AbstractModelFactory fModelFactory;
    private final ObjectsDirectory fSharedObjectsDirectory;
    private final Behavior fBehavior;
    private final Config fBehaviorConfig;
    private final String fStateName;
    private final String fBehaviorName;

    /**
     * @param modelFactory so it can create the behavior associated with the state
     * @param name of the state
     * @param config for the state
     * @param objectsDirectory so it can get the behavior associated with the state if it already exists
     */

    public SingleState(AbstractModelFactory modelFactory, String name, Config config, ObjectsDirectory objectsDirectory) {
        fModelFactory = modelFactory;
        fSharedObjectsDirectory = objectsDirectory;
        fStateName = name;

        //Reads in the behavior and behavior config for the state
        fBehaviorName = config.getString("behavior");
        if (config.contains("behavior_config")) {
            fBehaviorConfig = config.getSubConfig("behavior_config", "behavior_config");
        } else {
            fBehaviorConfig = new Config("behavior_config", new HashMap<>());
        }

        // Only create a new behavior class instance if it has not already been created by another state
        // A single instance allows all states using this behvavior class to share member variable information inside the single instance
        // Behavior.Intialize() is called each time a new state is entered and Behavior.Dispose() is called when leaving the state
        @Nullable
        Behavior behavior = fSharedObjectsDirectory.getBehaviorObject(fBehaviorName);

        if (behavior == null) {
            behavior = fModelFactory.createBehavior(fBehaviorName, fBehaviorConfig);
            fSharedObjectsDirectory.setBehaviorObject(fBehaviorName, behavior);
        }

        fBehavior = behavior;
    }

    /**
     * @return the state
     */

    @Override
    public Set<State> getSubStates() {
        return Sets.of(this);
    }

    /**
     * call initialize on the behavior
     */

    @Override
    public void initialize() {
        fBehavior.initialize(fStateName, fBehaviorConfig);
    }

    /**
     * Call update on the behavior
     */

    @Override
    public void update() {
        fBehavior.update();
    }

    /**
     * Call dispose on the behavior
     */

    @Override
    public void dispose() {
        fBehavior.dispose();
    }

    /**
     * @return the isDone from the behavior
     */

    @Override
    public boolean isDone() {
        return fBehavior.isDone();
    }

    /**
     * @return the subsystem used by the state
     */

    @Override
    public Set<String> getSubsystems() {
        return fBehavior.getSubsystems();
    }

    /**
     * @return the name of the state
     */

    @Override
    public String getName() {
        return fStateName;
    }

    /**
     * This method is used so the .toString will return the name of the state if called on this object
     * @return the name of the state
     */

    @Override
    public String toString() {
        return getName();
    }
}
