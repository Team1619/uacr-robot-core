package org.uacr.events.sim;

public class SimInputBooleanSetEvent {

	public final String name;
	public final boolean value;

	public SimInputBooleanSetEvent(String name, boolean value) {
		this.name = name;
		this.value = value;
	}
}
