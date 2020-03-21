package org.uacr.services.states;

import org.uacr.robot.RobotManager;
import org.uacr.robot.AbstractStateControls;
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
    private final ObjectsDirectory fSharedObjectsDirectory;
    private final InputValues fSharedInputValues;
    private final FMS fFms;
    private final YamlConfigParser fParser;
    private final RobotConfiguration fRobotConfiguration;
    private double mPreviousTime;
    private long mFrameTimeThreshold;

    private FMS.Mode fCurrentFmsMode;
    private StateMachine fStateMachine;
    private RobotManager fRobotManager;

    @Inject
    public StatesService(InputValues inputValues, FMS fms, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory, AbstractStateControls stateControls) {
        fParser = new YamlConfigParser();
        fSharedObjectsDirectory = objectsDirectory;
        fSharedInputValues = inputValues;
        fFms = fms;
        fCurrentFmsMode = fFms.getMode();
        fRobotConfiguration = robotConfiguration;
        fRobotManager = new RobotManager(fSharedInputValues, fRobotConfiguration, stateControls);
        fStateMachine = new StateMachine(fSharedObjectsDirectory, fRobotManager, fRobotConfiguration, fSharedInputValues);
    }

    /**
     * Called when the code is stated up by the abstractSchedulerService
     *
     * @throws Exception if it does not start up correctly
     */
    @Override
    public void startUp() throws Exception {
        sLogger.debug("Starting StatesService");

        fParser.loadWithFolderName("states.yaml");
        fSharedObjectsDirectory.registerAllStates(fParser);

        mPreviousTime = System.currentTimeMillis();
        mFrameTimeThreshold = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_state_service");

        fSharedInputValues.setBoolean("ipb_robot_has_been_zeroed", false);

        sLogger.debug("StatesService started");
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
        if (nextFmsMode != fCurrentFmsMode) {

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
                if (fCurrentFmsMode != FMS.Mode.AUTONOMOUS) {
                    fSharedInputValues.setBoolean("ipb_robot_has_been_zeroed", false);
                }
                fSharedInputValues.setBoolean("ipb_auto_complete", false);
            }
        }

        //Update the current RobotManger and update the StateMachine
        fCurrentFmsMode = nextFmsMode;
        if (fCurrentFmsMode != FMS.Mode.DISABLED) {
            fRobotManager.update();
            fStateMachine.update();
        }

        // Check for delayed frames
        double currentTime = System.currentTimeMillis();
        double frameTime = currentTime - frameStartTime;
        double totalCycleTime = currentTime - mPreviousTime;
        fSharedInputValues.setNumeric("ipn_frame_time_states_service", frameTime);
        if (frameTime > mFrameTimeThreshold) {
            sLogger.debug("********** States Service frame time = {}", frameTime);
        }
        mPreviousTime = currentTime;

    }

    @Override
    public void shutDown() throws Exception {

    }
}
