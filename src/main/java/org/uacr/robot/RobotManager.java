package org.uacr.robot;

import org.uacr.models.state.State;
import org.uacr.shared.abstractions.FMS;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import javax.annotation.Nullable;

/**
 * Manages current control mode, isReady and isDone logic, and the status of the robot
 */

public class RobotManager {

    private static final Logger sLogger = LogManager.getLogger(RobotManager.class);

    protected final InputValues fSharedInputValues;
    protected final RobotConfiguration fRobotConfiguration;
    private AbstractStateControls fStateControls;
    @Nullable
    private AbstractModeLogic fLastModeLogic;

    public RobotManager(InputValues inputValues, RobotConfiguration robotConfiguration, AbstractStateControls stateControls) {
        fSharedInputValues = inputValues;
        fRobotConfiguration = robotConfiguration;
        fStateControls = stateControls;
        fLastModeLogic = null;
    }

    /**
     * Called when switching into Teleop or Auto
     */
    public void initialize(FMS.Mode currentFmsMode) {
        // Initializes the robot status
        fStateControls.getRobotStatus().initialize();

        // Initializes and runs a frame of state controls to select the correct robot control mode
        fStateControls.initialize(currentFmsMode);
        fStateControls.update();
    }


    public void disabledUpdate() {
        fStateControls.getRobotStatus().disabledUpdate();
    }

    /**
     * Called every frame to update robot status and current mode logic
     */
    public void update() {
        // Updates the robot status
        fStateControls.getRobotStatus().update();

        // Updates state controls to select the correct mode logic
        fStateControls.update();

        // Gets the current mode logic from state controls
        AbstractModeLogic currentModeLogic = fStateControls.getCurrentModeLogic();

        // If the requested mode logic has changed dispose the old mode and initialize the new mode logic
        if (currentModeLogic != fLastModeLogic) {
            // Dispose the old mode logic if it exists
            if (fLastModeLogic != null) {
                fLastModeLogic.dispose();
            }

            // Initialize the new mode logic
            currentModeLogic.initialize();
        }

        // Updated the current mode logic
        currentModeLogic.update();

        // Set the last mode logic to the current mode logic
        fLastModeLogic = currentModeLogic;
    }

    /**
     * Called when switching between Auto, Teleop and Disabled
     */
    public void dispose() {
        sLogger.info("Leaving {} mode", fStateControls.getCurrentControlMode().toString());

        // Disposes the robot status
        fStateControls.getRobotStatus().dispose();

        // Disposes state controls
        fStateControls.dispose();

        // Disposes the current mode logic
        if (fLastModeLogic != null) {
            fLastModeLogic.dispose();
        }

        // Set the last mode logic to null so when the next mode logic is selected it will be initialized
        fLastModeLogic = null;
    }

    /**
     * Defines when each state is ready
     *
     * @param name the name of the state being checked if it's ready
     * @return whether the state is ready
     */
    public final boolean isReady(String name) {
        // Calls isReady on the current mode logic based on the mode selected by state controls
        return fStateControls.getCurrentModeLogic().isReady(name);
    }

    /**
     * Determines when a state is done
     * This is a place to override the isDone logic in the behavior for a specific state
     *
     * @param name  the state being checked if it's done
     * @param state the state object (used to call the behavior's isDone)
     * @return Whether the state is done
     */
    public final boolean isDone(String name, State state) {
        // Calls isDone on the current mode logic based on the mode selected by state controls
        return fStateControls.getCurrentModeLogic().isDone(name, state);
    }
}
