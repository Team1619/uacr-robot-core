package org.uacr.models.inputs.bool;

import org.uacr.utilities.Config;

public abstract class InputBoolean {

	protected final Object fName;
	protected final boolean fIsInverted;

	public InputBoolean(Object name, Config config) {
		fName = name;
		fIsInverted = config.getBoolean("inverted", false);
	}

	public abstract void initialize();

	public abstract void update();

	public abstract boolean get();

	public abstract DeltaType getDelta();

	public abstract void processFlag(String flag);

	public enum DeltaType {
		RISING_EDGE,
		FALLING_EDGE,
		NO_DELTA
	}
}
