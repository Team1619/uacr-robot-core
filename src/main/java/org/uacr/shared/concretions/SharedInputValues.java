package org.uacr.shared.concretions;

import org.uacr.shared.abstractions.InputValues;
import org.uacr.utilities.injection.Singleton;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SharedInputValues implements InputValues {

	private static final Logger sLogger = LogManager.getLogger(SharedInputValues.class);

	private Map<String, Boolean> fInputBooleans = new ConcurrentHashMap<>();
	private Map<String, Boolean> fInputBooleanRisingEdges = new ConcurrentHashMap<>();
	private Map<String, Boolean> fInputBooleanFallingEdges = new ConcurrentHashMap<>();
	private Map<String, Double> fInputNumerics = new ConcurrentHashMap<>();
	private Map<String, Map<String, Double>> fInputVectors = new ConcurrentHashMap<>();
	private Map<String, String> fInputStrings = new ConcurrentHashMap<>();
	private Map<String, String> fInputFlags = new ConcurrentHashMap<>();

	@Override
	public boolean getBoolean(String name) {
		return fInputBooleans.getOrDefault(name, false);
	}

	@Override
	public boolean getBooleanRisingEdge(String name) {
		boolean risingEdge = fInputBooleanRisingEdges.getOrDefault(name, false);
		// Rising edge will remain for a frame
		// Comment in to use single use rising edge
		//fBooleanRisingEdgeInputs.put(name, false);
		return risingEdge;
	}

	@Override
	public boolean getBooleanFallingEdge(String name) {
		boolean fallingEdge = fInputBooleanFallingEdges.getOrDefault(name, false);
		// Falling edge will remain for a frame
		// Comment in to use single use falling edge
		//fBooleanFallingEdgeInputs.put(name, false);
		return fallingEdge;
	}

	@Override
	public Map<String, Boolean> getAllBooleans() {
		return fInputBooleans;
	}

	@Override
	public double getNumeric(String name) {
		return fInputNumerics.getOrDefault(name, 0.0);
	}

	@Override
	public Map<String, Double> getAllNumerics() {
		return fInputNumerics;
	}

	@Override
	public Map<String, Double> getVector(String name) {
		return fInputVectors.getOrDefault(name, new HashMap<>());
	}

	@Override
	public void setInputFlag(String name, String flag) {
		fInputFlags.put(name, flag);
	}

	@Override
	public String getInputFlag(String name) {
		String flag = fInputFlags.getOrDefault(name, "none");
		fInputFlags.remove(name);
		return flag;
	}

	@Override
	public Map<String, Map<String, Double>> getAllVectors() {
		return fInputVectors;
	}

	@Override
	public String getString(String name) {
		return fInputStrings.getOrDefault(name, "DoesNotExist");
	}

	@Override
	public Map<String, String> getAllStrings() {
		return fInputStrings;
	}

	public boolean setBoolean(String name, boolean value) {
		//sLogger.trace("Setting boolean input '{}' to {}", name, value);

		fInputBooleans.put(name, value);
		return value;
	}

	public void setBooleanRisingEdge(String name, boolean value) {
		//sLogger.trace("Setting boolean rising edge input '{}' to {}", name, value);
		fInputBooleanRisingEdges.put(name, value);
	}

	@Override
	public void setBooleanFallingEdge(String name, boolean value) {
		fInputBooleanFallingEdges.put(name, value);
	}

	@Override
	public void setNumeric(String name, double value) {
		//sLogger.trace("Setting numeric input '{}' to {}", name, value);

		fInputNumerics.put(name, value);
	}

	@Override
	public void setVector(String name, Map<String, Double> values) {
		//sLogger.trace("Setting vector {}", name);

		fInputVectors.put(name, values);
	}

	@Override
	public void setString(String name, String value) {
		fInputStrings.put(name, value);
	}
}
