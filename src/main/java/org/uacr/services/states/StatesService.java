package org.uacr.services.states;

import org.uacr.robot.AbstractStateControls;
import org.uacr.robot.RobotManager;
import org.uacr.shared.abstractions.FMS;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.services.ScheduledService;
import org.uacr.utilities.services.Scheduler;

/**
 * Reads the FMS mode sent to us by the field and runs the correct StateControls and StateMachine
 */

public class StatesService implements ScheduledService {

    private static final Logger sLogger = LogManager.getLogger(StatesService.class);

    private final InputValues fSharedInputValues;
    private final ObjectsDirectory fSharedObjectsDirectory;
    private final FMS fFms;
    private final YamlConfigParser fStatesParser;
    private final RobotConfiguration fRobotConfiguration;
    private final StateMachine fStateMachine;
    private final RobotManager fRobotManager;

    private FMS.Mode mCurrentFmsMode;
    private long mFrameTimeThreshold;

    /**
     * @param inputValues the map that holds the values from all the inputs
     * @param fms holds the  value of the current FMS mode (Auto, Teleop)
     * @param robotConfiguration passed into RobotManager and StateMachine as well as used to read in general config values
     * @param objectsDirectory objectsDirectory used to store the state objects
     * @param stateControls used to control the state of the robot such as modes controlled by the drivers (passed into the robot manager)
     */

    @Inject
    public StatesService(InputValues inputValues, FMS fms, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory, AbstractStateControls stateControls) {
        fSharedInputValues = inputValues;
        fSharedObjectsDirectory = objectsDirectory;
        fFms = fms;
        fStatesParser = new YamlConfigParser();
        fRobotConfiguration = robotConfiguration;
        fRobotManager = new RobotManager(fSharedInputValues, fRobotConfiguration, stateControls);
        fStateMachine = new StateMachine(fSharedObjectsDirectory, fRobotManager, fRobotConfiguration, fSharedInputValues);

        mCurrentFmsMode = fFms.getMode();
        mFrameTimeThreshold = -1;
    }

    /**
     * Called when the code is stated up by the abstractSchedulerService
     *
     * @throws Exception if it does not start up correctly
     */
    @Override
    public void startUp() throws Exception {
        sLogger.trace("Starting StatesService");

        mFrameTimeThreshold = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_state_service");

        fStatesParser.loadWithFolderName("states.yaml");
        fSharedObjectsDirectory.registerAllStates(fStatesParser);

        fSharedInputValues.setBoolean("ipb_robot_has_been_zeroed", false);

        sLogger.trace("StatesService started");
    }

    /**
     * Determines the frame rate for this service
     *
     * @return the frame rate
     */
    @Override
    public Scheduler scheduler() {
        return new Scheduler(1000 / 60);
    }

    /**
     * Called every frame by the abstractSchedulerService based on the frame time set in scheduler() above
     * Decides what mode we are running (Auto, Teleop, Disabled)
     * Updates the instance of StateControls and the StateMachine
     */
    @Override
    public void runOneIteration() throws Exception {

        double frameStartTime = System.currentTimeMillis();

        //Get the FMS mode from the field or webDashboard
        FMS.Mode nextFmsMode = fFms.getMode();

        //If mode is changing
        if (nextFmsMode != mCurrentFmsMode) {

            // Initialize StateMachine with either Teleop or Auto StateControls
            if (nextFmsMode == FMS.Mode.AUTONOMOUS) {
                fSharedInputValues.setBoolean("ipb_robot_has_been_zeroed", false);
                fRobotManager.initialize(nextFmsMode);
                fStateMachine.initialize();
            } else if (nextFmsMode == FMS.Mode.TELEOP) {
                fRobotManager.initialize(nextFmsMode);
                fStateMachine.initialize();
            } else if (nextFmsMode == FMS.Mode.DISABLED) {
                fRobotManager.dispose();
                fStateMachine.dispose();
                if (mCurrentFmsMode != FMS.Mode.AUTONOMOUS) {
                    fSharedInputValues.setBoolean("ipb_robot_has_been_zeroed", false);
                }
                fSharedInputValues.setBoolean("ipb_auto_complete", false);
            }
        }

        //Update the current RobotManger and update the StateMachine
        mCurrentFmsMode = nextFmsMode;
        if (mCurrentFmsMode != FMS.Mode.DISABLED) {
            fRobotManager.update();
            fStateMachine.update();
        }

        // Check for delayed frames
        double currentTime = System.currentTimeMillis();
        double frameTime = currentTime - frameStartTime;
        fSharedInputValues.setNumeric("ipn_frame_time_states_service", frameTime);
        if (frameTime > mFrameTimeThreshold) {
            sLogger.debug("********** States Service frame time = {}", frameTime);
        }
    }

    /**
     * Shuts down the StateService
     * Currently not used for any actions
     * @throws Exception
     */

    @Override
    public void shutDown() throws Exception {

    }
}
