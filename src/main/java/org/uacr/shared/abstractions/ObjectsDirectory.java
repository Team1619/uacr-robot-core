package org.uacr.shared.abstractions;

import org.uacr.models.behavior.Behavior;
import org.uacr.models.inputs.bool.InputBoolean;
import org.uacr.models.inputs.numeric.InputNumeric;
import org.uacr.models.inputs.vector.InputVector;
import org.uacr.models.outputs.bool.OutputBoolean;
import org.uacr.models.outputs.numeric.OutputNumeric;
import org.uacr.models.state.State;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;

import javax.annotation.Nullable;

public interface ObjectsDirectory {

    // Inputs
    void registerAllInputs(YamlConfigParser inputBooleansParser, YamlConfigParser inputNumericsParser, YamlConfigParser inputVectorsParser);

    void registerInputBoolean(String name, Config config);

    void registerInputNumeric(String name, Config config);

    void registerInputVector(String name, Config config);

    InputBoolean getInputBooleanObject(String name);

    InputNumeric getInputNumericObject(String name);

    InputVector getInputVectorObject(String name);


    // Behaviors
    void setBehaviorObject(String name, Behavior behavior);

    @Nullable
    Behavior getBehaviorObject(String name);


    // States
    void registerAllStates(YamlConfigParser parser);

    void registerStates(String name, YamlConfigParser statesParser, Config config);

    State getStateObject(String name);

    void setStateObject(String name, State state);


    // Outputs
    void registerAllOutputs(YamlConfigParser outputNumericsParser, YamlConfigParser outputBooleansParser);

    void registerOutputNumeric(String name, Config config, YamlConfigParser parser);

    void registerOutputBoolean(String name, Config config, YamlConfigParser parser);

    OutputBoolean getOutputBooleanObject(String name);

    OutputNumeric getOutputNumericObject(String name);


    // Hardware Objects
    void setHardwareObject(Object deviceNumber, Object hardwareObject);

    @Nullable
    Object getHardwareObject(Object deviceNumber);
}
