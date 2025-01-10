package io.openems.edge.controller.io.loadshedding;

import io.openems.common.types.OptionsEnum;

public enum StartupBehavior implements OptionsEnum {
	UNDEFINED(-1, "Undefined"),
	ALL_ON(0, "All loads on at startup"),
	ALL_OFF(1, "All loads off at startup");
	
	private final int value;
	private final String name;
	
	private StartupBehavior(int value, String name) {
		this.value = value;
		this.name = name;
	}
	
	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}
}
