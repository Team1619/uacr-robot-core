package org.uacr.services.output;

import org.uacr.models.outputs.bool.OutputBoolean;
import org.uacr.models.outputs.numeric.OutputNumeric;
import org.uacr.robot.AbstractModelFactory;
import org.uacr.shared.abstractions.*;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.services.ScheduledService;
import org.uacr.utilities.services.Scheduler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Initializes and updates all outputs using values from SharedOutputValues
 */

public class OutputService implements ScheduledService {

    private static final Logger sLogger = LogManager.getLogger(OutputService.class);

    private final FMS fFms;
    private final InputValues fSharedInputValues;
    private final OutputValues fSharedOutputValues;
    private final ObjectsDirectory fSharedOutputsDirectory;
    private final RobotConfiguration fRobotConfiguration;
    private final YamlConfigParser fOutputNumericsParser;
    private final YamlConfigParser fOutputBooleansParser;

    private FMS.Mode mCurrentFmsMode;
    private Set<String> mOutputNumericNames;
    private Set<String> mOutputBooleanNames;
    private long mFrameTimeThreshold;

    @Inject
    public OutputService(AbstractModelFactory modelFactory, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
        fFms = fms;
        fSharedInputValues = inputValues;
        fSharedOutputValues = outputValues;
        fRobotConfiguration = robotConfiguration;
        fSharedOutputsDirectory = objectsDirectory;
        fOutputNumericsParser = new YamlConfigParser();
        fOutputBooleansParser = new YamlConfigParser();

        mCurrentFmsMode = FMS.Mode.DISABLED;
        mOutputNumericNames = new HashSet<>();
        mOutputBooleanNames = new HashSet<>();
        mFrameTimeThreshold = -1;
    }

    @Override
    public void startUp() throws Exception {
        sLogger.trace("Starting OutputService");

        mOutputNumericNames = fRobotConfiguration.getOutputNumericNames();
        mOutputBooleanNames = fRobotConfiguration.getOutputBooleanNames();
        mFrameTimeThreshold = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_output_service");

        fOutputNumericsParser.loadWithFolderName("output-numerics.yaml");
        fOutputBooleansParser.loadWithFolderName("output-booleans.yaml");
        fSharedOutputsDirectory.registerAllOutputs(fOutputNumericsParser, fOutputBooleansParser);

        sLogger.trace("OutputService started");
    }

    @Override
    public void runOneIteration() throws Exception {

        long frameStartTime = System.currentTimeMillis();

        FMS.Mode nextFmsMode = fFms.getMode();

        if (mCurrentFmsMode == FMS.Mode.DISABLED && nextFmsMode != FMS.Mode.DISABLED) {
            mOutputBooleanNames.stream().map(fSharedOutputsDirectory::getOutputNumericObject).forEachOrdered(OutputNumeric::initialize);
            mOutputBooleanNames.stream().map(fSharedOutputsDirectory::getOutputBooleanObject).forEachOrdered(OutputBoolean::initialize);
        }

        mCurrentFmsMode = nextFmsMode;

        for (String outputNumericName : mOutputNumericNames) {
            OutputNumeric outputNumericObject = fSharedOutputsDirectory.getOutputNumericObject(outputNumericName);
            Map<String, Object> outputNumericOutputs = fSharedOutputValues.getOutputNumericValue(outputNumericName);
            outputNumericObject.processFlag(fSharedOutputValues.getOutputFlag(outputNumericName));
            outputNumericObject.setHardware((String) outputNumericOutputs.get("type"), (double) outputNumericOutputs.get("value"), (String) outputNumericOutputs.get("profile"));
        }
        for (String outputBooleanName : mOutputBooleanNames) {
            OutputBoolean outputBooleanObject = fSharedOutputsDirectory.getOutputBooleanObject(outputBooleanName);
            outputBooleanObject.processFlag(fSharedOutputValues.getOutputFlag(outputBooleanName));
            outputBooleanObject.setHardware(fSharedOutputValues.getBoolean(outputBooleanName));
        }

        // Check for delayed frames
        long currentTime = System.currentTimeMillis();
        long frameTime = currentTime - frameStartTime;
        fSharedInputValues.setNumeric("ipn_frame_time_output_service", frameTime);
        if (frameTime > mFrameTimeThreshold) {
            sLogger.debug("********** Output Service frame time = {}", frameTime);
        }
    }

    @Override
    public void shutDown() throws Exception {

    }

    @Override
    public Scheduler scheduler() {
        return new Scheduler(1000 / 60);
    }
}
