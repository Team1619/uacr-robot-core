package org.uacr.shared.abstractions;

public interface FMS {

    Mode getMode();

    void setMode(Mode mode);

    enum Mode {
        AUTONOMOUS,
        TELEOP,
        DISABLED,
        TEST
    }
}
