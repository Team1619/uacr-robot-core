package org.uacr.robot;

import org.uacr.services.input.InputService;
import org.uacr.services.output.OutputService;
import org.uacr.services.states.StatesService;
import org.uacr.shared.abstractions.*;
import org.uacr.shared.concretions.*;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;
import org.uacr.utilities.services.ScheduledMultiService;
import org.uacr.utilities.services.Scheduler;
import org.uacr.utilities.services.Service;
import org.uacr.utilities.services.managers.AsyncServiceManager;
import org.uacr.utilities.services.managers.ServiceManager;

import java.util.List;

public abstract class RobotCore {

    private static final Logger sLogger = LogManager.getLogger(RobotCore.class);

    protected final FMS fFms;
    protected final RobotConfiguration fRobotConfiguration;
    protected final InputValues fInputValues;
    protected final OutputValues fOutputValues;
    protected final HardwareFactory fHardwareFactory;
    protected final EventBus fEventBus;
    protected final ObjectsDirectory fObjectsDirectory;
    protected final AbstractStateControls fStateControls;
    protected final ServiceManager fServiceManager;
    protected final AbstractModelFactory fModelFactory;

    protected RobotCore() {
        YamlConfigParser parser = new YamlConfigParser();
        parser.load("general.yaml");

        Config loggerConfig = parser.getConfig("logger");
        if (loggerConfig.contains("log_level")) {
            LogManager.setLogLevel(loggerConfig.getEnum("log_level", LogManager.Level.class));
        }

        fFms = new SharedFMS();
        fRobotConfiguration = new SharedRobotConfiguration();
        fInputValues = new SharedInputValues();
        fOutputValues = new SharedOutputValues();
        fHardwareFactory = new SharedHardwareFactory();
        fEventBus = new SharedEventBus();
        fObjectsDirectory = new SharedObjectsDirectory();
        fStateControls = createStateControls();

        fModelFactory = createModelFactory();

        StatesService statesService = new StatesService(fModelFactory, fInputValues, fFms, fRobotConfiguration,
                fObjectsDirectory, fStateControls);

        InputService inputService = new InputService(fModelFactory, fInputValues, fRobotConfiguration,
                fObjectsDirectory);

        OutputService outputService = new OutputService(fModelFactory, fFms, fInputValues,
                fOutputValues, fRobotConfiguration, fObjectsDirectory);

        fServiceManager = new AsyncServiceManager(
                new ScheduledMultiService(new Scheduler(10), inputService, statesService, outputService),
                new ScheduledMultiService(new Scheduler(30), createInfoServices()));
    }

    protected abstract AbstractStateControls createStateControls();

    protected abstract AbstractModelFactory createModelFactory();

    protected abstract List<Service> createInfoServices();

    public void start() {
        sLogger.info("Starting services");
        fServiceManager.start();
        fServiceManager.awaitHealthy();
        sLogger.info("********************* ALL SERVICES STARTED *******************************");
    }

    public FMS getFms() {
        return fFms;
    }
}
