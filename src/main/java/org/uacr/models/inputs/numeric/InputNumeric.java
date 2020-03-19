package org.uacr.models.inputs.numeric;

import org.uacr.utilities.Config;

public abstract class InputNumeric {

	protected final Object fName;
	protected final boolean fIsInverted;

	public InputNumeric(Object name, Config config) {
		fName = name;
		fIsInverted = config.getBoolean("inverted", false);
	}

	public abstract void initialize();

	public abstract void update();

	public abstract double get();

	public abstract double getDelta();

	public abstract void processFlag(String flag);
}


