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
    private long mPreviousTime;
    private long mFrameTimeThreshold;
    private long mFrameCycleTimeThreshold;

    /**
     * @param modelFactory the ModelFactory to be used (currently not used)
     * @param inputValues the map that holds the values from all the inputs
     * @param robotConfiguration used to obtain a list of all the inputs to be created as well as other configuration information used by the InputService
     * @param objectsDirectory used to store the input objects
     */

    @Inject
    public InputService(AbstractModelFactory modelFactory, InputValues inputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
        fSharedInputValues = inputValues;
        fSharedObjectsDirectory = objectsDirectory;
        fRobotConfiguration = robotConfiguration;
        fInputBooleanParser = new YamlConfigParser();
        fInputNumericParser = new YamlConfigParser();
        fInputVectorParser = new YamlConfigParser();

        mInputBooleanNames = new HashSet<>();
        mInputNumericNames = new HashSet<>();
        mInputVectorNames = new HashSet<>();
        mPreviousTime = -1;
        mFrameTimeThreshold = -1;
        mFrameCycleTimeThreshold = -1;
    }

    /**
     * Starts the input service
     * Obtains a list of all input objects (boolean, numeric and vector)
     * Loads the input ymal files
     * Registers the inputs with the objects directory (this creates them)
     * @throws Exception if the start up process does no succeed
     */

    @Override
    public void startUp() throws Exception {
        sLogger.trace("Starting InputService");

        mInputBooleanNames = fRobotConfiguration.getInputBooleanNames();
        mInputNumericNames = fRobotConfiguration.getInputNumericNames();
        mInputVectorNames = fRobotConfiguration.getInputVectorNames();
        mPreviousTime = System.currentTimeMillis();
        mFrameTimeThreshold = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_input_service");
        mFrameCycleTimeThreshold = fRobotConfiguration.getInt("global_timing", "frame_cycle_time_threshold_core_thread");

        fInputBooleanParser.loadWithFolderName("input-booleans.yaml");
        fInputNumericParser.loadWithFolderName("input-numerics.yaml");
        fInputVectorParser.loadWithFolderName("input-vectors.yaml");
        fSharedObjectsDirectory.registerAllInputs(fInputBooleanParser, fInputNumericParser, fInputVectorParser);

        fSharedInputValues.setString("active states", "");

        sLogger.trace("InputService started");
    }

    /**
     * Runs every frame
     * Loops through all inputs and updates their values in SharedInputValues
     * Monitors for long frame times
     * @throws Exception if it does not run cleanly
     */

    @Override
    public void runOneIteration() throws Exception {

        long frameStartTime = System.currentTimeMillis();

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
        long currentTime = System.currentTimeMillis();
        long frameTime = currentTime - frameStartTime;
        long totalCycleTime = frameStartTime - mPreviousTime;
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

    /**
     * Shuts down the InputService
     * Currently preforms no actions
     * @throws Exception
     */

    @Override
    public void shutDown() throws Exception {

    }

    /**
     * @return a new Scheduler class with the desired frame duration
     */

    @Override
    public Scheduler scheduler() {
        return new Scheduler(1000 / 60);
    }
}
