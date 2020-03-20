package org.uacr.robot;

import org.uacr.models.exceptions.ConfigurationException;
import org.uacr.shared.abstractions.FMS;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * The base for all state control logic
 */

public abstract class StateControls {

    private static final Logger sLogger = LogManager.getLogger(StateControls.class);

    protected final InputValues fSharedInputValues;
    protected final RobotConfiguration fRobotConfiguration;
    private final HashMap<ControlMode, ModeLogic> fModeLogicMap;
    protected FMS.Mode fFmsMode;
    private ControlMode fCurrentControlMode;
    @Nullable
    private RobotStatus fRobotStatus;

    public StateControls(InputValues inputValues, RobotConfiguration robotConfiguration) {
        fSharedInputValues = inputValues;
        fRobotConfiguration = robotConfiguration;
        fModeLogicMap = new HashMap<>();
        fFmsMode = FMS.Mode.DISABLED;
    }

    /**
     * Called when switching into Teleop or Auto
     * This is a place clear and initialize variables...
     */
    public abstract void initialize(FMS.Mode currentFmsMode);

    /**
     * Called every frame
     * This is a place read buttons, check modes...
     */
    public abstract void update();

    /**
     * Called when switching between Auto, Teleop and Disabled
     * This is a place for any clean up, clearing variables...
     */
    public abstract void dispose();

    /**
     * Called by the concretion of StateControls to initialize the mode logic for each control mode
     *
     * @param controlMode the ControlMode that is mode logic should be used for
     * @param modeLogic   the ModeLogic concretion to be used for the ControlMode
     */
    public void registerModeLogic(ControlMode controlMode, ModeLogic modeLogic) {
        fModeLogicMap.put(controlMode, modeLogic);
    }

    /**
     * Called by the concretion of StateControls to initialize the robot status for this robot
     *
     * @param robotStatus the RobotStatus concretion to be used for this robot
     */
    public void registerRobotStatus(RobotStatus robotStatus) {
        fRobotStatus = robotStatus;
    }

    /**
     * Called by the RobotManager to get the current ModeLogic concretion selected by the StateControls concretion
     *
     * @return the current ModeLogic concretion
     */
    public ModeLogic getCurrentModeLogic() {
        return fModeLogicMap.get(getCurrentControlMode());
    }

    /**
     * Called by the RobotManager to get the RobotStatus for this robot
     *
     * @return the RobotStatus for this robot
     */
    public RobotStatus getRobotStatus() {
        if (fRobotStatus != null) {
            return fRobotStatus;
        }
        throw new ConfigurationException("RobotStatus not registered. Call method registerRobotStatus() in the constructor of the concretion of StateControls");
    }

    /**
     * Called by RobotManager to get the current requested control mode
     *
     * @return the current control mode
     */
    public ControlMode getCurrentControlMode() {
        return fCurrentControlMode;
    }

    /**
     * Called by the StateControls concretion to select a ControlMode
     *
     * @param currentControlMode
     */
    public void setCurrentControlMode(ControlMode currentControlMode) {

        if (fModeLogicMap.get(currentControlMode) == null) {
            sLogger.error("******** No mode logic specified for " + currentControlMode + " ********");
        } else {
            fCurrentControlMode = currentControlMode;
        }
    }
}