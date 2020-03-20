package org.uacr.shared.concretions;

import org.uacr.shared.abstractions.FMS;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SharedFMS implements FMS {

    private static final Logger sLogger = LogManager.getLogger(SharedFMS.class);

    private Map<String, Mode> fData = new ConcurrentHashMap<>();

    @Override
    public Mode getMode() {
        return fData.getOrDefault("mode", Mode.DISABLED);
    }

    @Override
    public void setMode(Mode mode) {
        fData.put("mode", mode);

        sLogger.debug("FMS mode set to '{}'", mode);
    }
}
