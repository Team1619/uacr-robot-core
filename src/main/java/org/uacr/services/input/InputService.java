package org.uacr.services.input;

import org.uacr.models.inputs.bool.InputBoolean;
import org.uacr.models.inputs.numeric.InputNumeric;
import org.uacr.models.inputs.vector.InputVector;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.services.ScheduledService;
import org.uacr.utilities.services.Scheduler;

import java.util.HashSet;
import java.util.Set;

/**
 * Initializes and updates all inputs using values from SharedInputValues
 */

public class InputService implements ScheduledService {

    private static final Logger sLogger = LogManager.getLogger(InputService.class);

    private final InputValues fSharedInputValues;
    private final ObjectsDirectory fSharedObjectsDirectory;
    private final RobotConfiguration fRobotConfiguration;
    private final YamlConfigParser fInputBooleanParser;
    private final YamlConfigParser fInputNumericParser;
    private final YamlConfigParser fInputVectorParser;
    private Set<String> mInputBooleanNames;
    private Set<String> mInputNumericNames;
    private Set<String> mInputVectorNames;
    private double mPreviousTime;
    private long mFrameTimeThreshold;
    private long mFrameCycleTimeThreshold;

    @Inject
    public InputService(AbstractModelFactory modelFactory, InputValues inputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
        fSharedInputValues = inputValues;
        fRobotConfiguration = robotConfiguration;
        fSharedObjectsDirectory = objectsDirectory;
        fInputBooleanParser = new YamlConfigParser();
        fInputNumericParser = new YamlConfigParser();
        fInputVectorParser = new YamlConfigParser();
        mInputBooleanNames = new HashSet<>();
        mInputNumericNames = new HashSet<>();
        mInputVectorNames = new HashSet<>();
    }

    @Override
    public void startUp() throws Exception {
        sLogger.debug("Starting InputService");

        fInputBooleanParser.loadWithFolderName("input-booleans.yaml");
        fInputNumericParser.loadWithFolderName("input-numerics.yaml");
        fInputVectorParser.loadWithFolderName("input-vectors.yaml");
        fSharedObjectsDirectory.registerAllInputs(fInputBooleanParser, fInputNumericParser, fInputVectorParser);

        mInputBooleanNames = fRobotConfiguration.getInputBooleanNames();
        mInputNumericNames = fRobotConfiguration.getInputNumericNames();
        mInputVectorNames = fRobotConfiguration.getInputVectorNames();

        mPreviousTime = System.currentTimeMillis();
        mFrameTimeThreshold = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_input_service");
        mFrameCycleTimeThreshold = fRobotConfiguration.getInt("global_timing", "frame_cycle_time_threshold_core_thread");

        fSharedInputValues.setString("active states", "");

        sLogger.debug("InputService started");
    }

    @Override
    public void runOneIteration() throws Exception {

        double frameStartTime = System.currentTimeMillis();

        for (String name : mInputBooleanNames) {
            InputBoolean inputBoolean = fSharedObjectsDirectory.getInputBooleanObject(name);
            inputBoolean.processFlag(fSharedInputValues.getInputFlag(name));
            inputBoolean.update();
            fSharedInputValues.setBoolean(name, inputBoolean.get());
            switch (inputBoolean.getDelta()) {
                case RISING_EDGE:
                    fSharedInputValues.setBooleanRisingEdge(name, true);
                    // Comment out to use single use rising edge
                    fSharedInputValues.setBooleanFallingEdge(name, false);
                    break;
                case FALLING_EDGE:
                    fSharedInputValues.setBooleanFallingEdge(name, true);
                    // Comment out to use single use falling edge
                    fSharedInputValues.setBooleanRisingEdge(name, false);
                    break;
                default:
                    // Comment out to use single use falling edge
                    fSharedInputValues.setBooleanFallingEdge(name, false);
                    // Comment out to use single use rising edge
                    fSharedInputValues.setBooleanRisingEdge(name, false);
                    break;
            }
        }

        //sLogger.trace("Updated boolean inputs");

        for (String name : mInputNumericNames) {
            InputNumeric inputNumeric = fSharedObjectsDirectory.getInputNumericObject(name);
            inputNumeric.processFlag(fSharedInputValues.getInputFlag(name));
            inputNumeric.update();
            fSharedInputValues.setNumeric(name, inputNumeric.get());
        }

        //sLogger.trace("Updated numeric inputs");

        for (String name : mInputVectorNames) {
            InputVector inputVector = fSharedObjectsDirectory.getInputVectorObject(name);
            inputVector.processFlag(fSharedInputValues.getInputFlag(name));
            inputVector.update();
            fSharedInputValues.setVector(name, inputVector.get());
        }

        //sLogger.trace("Updated vector inputs");

        // Check for delayed frames
        double currentTime = System.currentTimeMillis();
        double frameTime = currentTime - frameStartTime;
        double totalCycleTime = frameStartTime - mPreviousTime;
        fSharedInputValues.setNumeric("ipn_frame_time_input_service", frameTime);
        fSharedInputValues.setNumeric("ipn_frame_cycle_time_core_thread", totalCycleTime);
        if (frameTime > mFrameTimeThreshold) {
            sLogger.debug("********** Input Service frame time = {}", frameTime);
        }
        if (totalCycleTime > mFrameCycleTimeThreshold) {
            sLogger.debug("********** Core thread frame cycle time = {}", totalCycleTime);
        }
        mPreviousTime = frameStartTime;
    }

    @Override
    public void shutDown() throws Exception {

    }

    @Override
    public Scheduler scheduler() {
        return new Scheduler(1000 / 60);
    }
}
