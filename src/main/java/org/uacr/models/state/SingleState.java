package org.uacr.models.state;

import org.uacr.models.behavior.Behavior;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.utilities.Config;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;

/**
 * The base for all states
 * One implementation is created for each state listed under 'single_state' in the state ymal file
 * Creates one copy of the associated behavior and points all states that use it to the same instance
 * Watches after the associated behavior
 */

public class SingleState implements State {

    private static final Logger sLogger = LogManager.getLogger(SingleState.class);

    private final AbstractModelFactory fModelFactory;
    private final ObjectsDirectory fSharedObjectsDirectory;
    private final Behavior fBehavior;
    private final Config fBehaviorConfig;
    private final String fStateName;
    private final String fBehaviorName;

    public SingleState(AbstractModelFactory modelFactory, String name, Config config, ObjectsDirectory objectsDirectory) {
        fModelFactory = modelFactory;
        fSharedObjectsDirectory = objectsDirectory;
        fStateName = name;

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

    @Override
    public Set<State> getSubStates() {
        return Set.of(this);
    }

    @Override
    public void initialize() {
        fBehavior.initialize(fStateName, fBehaviorConfig);
    }

    @Override
    public void update() {
        fBehavior.update();
    }

    @Override
    public void dispose() {
        fBehavior.dispose();
    }

    @Override
    public boolean isDone() {
        return fBehavior.isDone();
    }

    @Override
    public Set<String> getSubsystems() {
        return fBehavior.getSubsystems();
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
