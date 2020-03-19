package org.uacr.events.sim;

public class SimInputNumericSetEvent {

	public final String name;
	public final double value;

	public SimInputNumericSetEvent(String name, double value) {
		this.name = name;
		this.value = value;
	}
}
