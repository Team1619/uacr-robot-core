package org.uacr.shared.abstractions;


import java.util.Map;

public interface OutputValues {

	// All
	Map<String, Object> getAllOutputs();

	void setOutputFlag(String name, String flag);

	String getOutputFlag(String name);

	// Output Numeric
	void setNumeric(String outputNumericName, String outputType, double outputValue);

	void setNumeric(String outputNumericName, String outputType, double outputValue, String flag);

	Map<String, Object> getOutputNumericValue(String outputNumericName);


	// Output Boolean
	void setBoolean(String outputBooleanName, boolean outputValue);

	boolean getBoolean(String outputBooleanName);
}
