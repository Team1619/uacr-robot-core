package org.uacr.robot;

import org.uacr.shared.abstractions.*;
import org.uacr.shared.concretions.*;

public abstract class AbstractModule extends org.uacr.utilities.injection.AbstractModule {

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

