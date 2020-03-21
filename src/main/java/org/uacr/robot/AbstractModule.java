package org.uacr.robot;

import org.uacr.shared.abstractions.*;
import org.uacr.shared.concretions.*;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

public abstract class AbstractModule extends org.uacr.utilities.injection.AbstractModule {
    private static final Logger sLogger = LogManager.getLogger(AbstractModule.class);

    protected void configure() {
        bind(EventBus.class, SharedEventBus.class);
        bind(InputValues.class, SharedInputValues.class);
        bind(OutputValues.class, SharedOutputValues.class);
        bind(FMS.class, SharedFMS.class);
        bind(RobotConfiguration.class, SharedRobotConfiguration.class);
        bind(ObjectsDirectory.class, SharedObjectsDirectory.class);

        configureModeSpecificConcretions();
    }

    public abstract void configureModeSpecificConcretions();
}

