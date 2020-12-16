package org.uacr.shared.concretions;

import org.uacr.models.behavior.Behavior;
import org.uacr.models.inputs.bool.InputBoolean;
import org.uacr.models.inputs.numeric.InputNumeric;
import org.uacr.models.inputs.vector.InputVector;
import org.uacr.models.outputs.bool.OutputBoolean;
import org.uacr.models.outputs.numeric.OutputNumeric;
import org.uacr.models.state.State;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the creation and use of objects (inputs, states, outputs, behaviors)
 * Does not handle hardware objects
 */

@Singleton
public class SharedObjectsDirectory implements ObjectsDirectory {

    private static final Logger sLogger = LogManager.getLogger(SharedObjectsDirectory.class);

    private final Map<String, InputBoolean> fInputBooleanObjects;
    private final Map<String, InputNumeric> fInputNumericObjects;
    private final Map<String, InputVector> fInputVectorObjects;
    private final Map<String, OutputNumeric> fOutputNumericObjects;
    private final Map<String, OutputBoolean> fOutputBooleanObjects;
    private final Map<Object, State> fStateObjects;
    private final Map<Object, Behavior> fBehaviorObjects;

    @Inject
    public SharedObjectsDirectory() {
        fInputBooleanObjects = new ConcurrentHashMap<>();
        fInputNumericObjects = new ConcurrentHashMap<>();
        fInputVectorObjects = new ConcurrentHashMap<>();
        fOutputNumericObjects = new ConcurrentHashMap<>();
        fOutputBooleanObjects = new ConcurrentHashMap<>();
        fStateObjects = new ConcurrentHashMap<>();
        fBehaviorObjects = new ConcurrentHashMap<>();
    }

    //--------------------------- Inputs ----------------------------------------//


    @Override
    public void registerInputBoolean(String name, InputBoolean inputBoolean) {
        fInputBooleanObjects.put(name, inputBoolean);
    }

    @Override
    public void registerInputNumeric(String name, InputNumeric inputNumeric) {
        fInputNumericObjects.put(name, inputNumeric);
    }

    @Override
    public void registerInputVector(String name, InputVector inputVector) {
        fInputVectorObjects.put(name, inputVector);
    }

    @Override
    public InputBoolean getInputBooleanObject(String name) {
        return fInputBooleanObjects.get(name);
    }

    @Override
    public InputNumeric getInputNumericObject(String name) {
        return fInputNumericObjects.get(name);
    }

    @Override
    public InputVector getInputVectorObject(String name) {
        return fInputVectorObjects.get(name);
    }

    //--------------------------- Behaviors ----------------------------------------//

    /**
     * Adds a behavior object to the BehaviorObjects map
     * @param name of the behavior object to be added
     * @param behavior the behavior object
     */
    @Override
    public void setBehaviorObject(String name, Behavior behavior) {
        fBehaviorObjects.put(name, behavior);
    }

    /**
     * @param name of the desired behavior object
     * @return the behavior object
     */

    @Override
    @Nullable
    public Behavior getBehaviorObject(String name) {
        return fBehaviorObjects.get(name);
    }

    //--------------------------- States ----------------------------------------//

    /**
     * Adds a state to the StateObjects map
     * @param name of state to be added
     * @param state the state object
     */

    @Override
    public void registerStateObject(String name, State state) {
        fStateObjects.put(name, state);
    }

    /**
     * @param name of desired state
     * @return the state object
     */

    @Override
    public State getStateObject(String name) {
        return fStateObjects.get(name);
    }

    //--------------------------- Outputs ----------------------------------------//

    @Override
    public void registerOutputBoolean(String name, OutputBoolean outputBoolean) {
        fOutputBooleanObjects.put(name, outputBoolean);
    }

    @Override
    public void registerOutputNumeric(String name, OutputNumeric outputNumeric) {
        fOutputNumericObjects.put(name, outputNumeric);
    }

    @Override
    public OutputBoolean getOutputBooleanObject(String outputBooleanName) {
        return fOutputBooleanObjects.get(outputBooleanName);
    }

    @Override
    public OutputNumeric getOutputNumericObject(String outputNumericName) {
        return fOutputNumericObjects.get(outputNumericName);
    }
}
