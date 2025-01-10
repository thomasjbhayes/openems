package io.openems.edge.controller.io.loadshedding;

import io.openems.common.types.OptionsEnum;

public enum ThresholdState implements OptionsEnum {
	
	UNDEFINED(-1, "Undefined"), //
	BELOW_SWITCH_ON_LOAD_THRESHOLD(0, "Power consumption below threshold required to switch on next load"),
	NO_ACTION(1, "Power consumption requires no action"),
	ABOVE_SWITCH_OFF_NEXT_LOAD_THRESHOLD(2, "Power consumption above threshold to switch on next load");
	
	private final int value;
	private final String name;
	
	private ThresholdState(int value, String name) {
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
