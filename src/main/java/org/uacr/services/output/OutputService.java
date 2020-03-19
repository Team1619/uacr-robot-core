package org.uacr.services.output;

import org.uacr.models.outputs.bool.OutputBoolean;
import org.uacr.models.outputs.numeric.OutputNumeric;
import org.uacr.robot.ModelFactory;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.OutputValues;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.services.ScheduledService;
import org.uacr.utilities.services.Scheduler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OutputService implements ScheduledService {

	private static final Logger sLogger = LogManager.getLogger(OutputService.class);

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final ObjectsDirectory fSharedOutputsDirectory;
	private final RobotConfiguration fRobotConfiguration;
	private final YamlConfigParser fOutputNumericsParser;
	private final YamlConfigParser fOutputBooleansParser;
	private Set<String> fOutputNumericNames;
	private Set<String> fOutputBooleanNames;
	private double fPreviousTime;
	private long FRAME_TIME_THRESHOLD;

	@Inject
	public OutputService(ModelFactory modelFactory, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fRobotConfiguration = robotConfiguration;
		fSharedOutputsDirectory = objectsDirectory;
		fOutputNumericsParser = new YamlConfigParser();
		fOutputBooleansParser = new YamlConfigParser();
		fOutputNumericNames = new HashSet<>();
		fOutputBooleanNames = new HashSet<>();
	}

	@Override
	public void startUp() throws Exception {
		sLogger.debug("Starting OutputService");

		fOutputNumericsParser.loadWithFolderName("output-numerics.yaml");
		fOutputBooleansParser.loadWithFolderName("output-booleans.yaml");
		fSharedOutputsDirectory.registerAllOutputs(fOutputNumericsParser, fOutputBooleansParser);

		fOutputNumericNames = fRobotConfiguration.getOutputNumericNames();
		fOutputBooleanNames = fRobotConfiguration.getOutputBooleanNames();

		fPreviousTime = System.currentTimeMillis();
		FRAME_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_output_service");

		sLogger.trace("OutputService started");
	}

	@Override
	public void runOneIteration() throws Exception {

		double frameStartTime = System.currentTimeMillis();

		for (String outputNumericName : fOutputNumericNames) {
			OutputNumeric outputNumericObject = fSharedOutputsDirectory.getOutputNumericObject(outputNumericName);
			Map<String, Object> outputNumericOutputs = fSharedOutputValues.getOutputNumericValue(outputNumericName);
			outputNumericObject.processFlag(fSharedOutputValues.getOutputFlag(outputNumericName));
			outputNumericObject.setHardware((String) outputNumericOutputs.get("type"), (double) outputNumericOutputs.get("value"), (String) outputNumericOutputs.get("profile"));
		}
		for (String outputBooleanName : fOutputBooleanNames) {
			OutputBoolean outputBooleanObject = fSharedOutputsDirectory.getOutputBooleanObject(outputBooleanName);
			outputBooleanObject.processFlag(fSharedOutputValues.getOutputFlag(outputBooleanName));
			outputBooleanObject.setHardware(fSharedOutputValues.getBoolean(outputBooleanName));
		}

		// Check for delayed frames
		double currentTime = System.currentTimeMillis();
		double frameTime = currentTime - frameStartTime;
		double totalCycleTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ipn_frame_time_output_service", frameTime);
		if (frameTime > FRAME_TIME_THRESHOLD) {
			sLogger.debug("********** Output Service frame time = {}", frameTime);
		}
		fPreviousTime = currentTime;

	}

	@Override
	public void shutDown() throws Exception {

	}

	@Override
	public Scheduler scheduler() {
		return new Scheduler(1000 / 60);
	}
}
