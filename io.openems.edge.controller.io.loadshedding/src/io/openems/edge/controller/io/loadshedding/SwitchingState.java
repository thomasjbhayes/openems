package io.openems.edge.controller.io.loadshedding;

import io.openems.common.types.OptionsEnum;

public enum SwitchingState implements OptionsEnum {
	UNDEFINED(-1, "Undefined"),
	ALL_LOADS_ON(0, "All loads switched on"),
	LOAD_1_SWITCHED_OFF(1, "Load 1 Switched Off"),
	LOAD_2_SWITCHED_OFF(2, "Load 2 Switched Off"),
	LOAD_3_SWITCHED_OFF(3, "Load 3 Switched Off"),
	LOAD_4_SWITCHED_OFF(4, "Load 4 Switched Off"),
	LOAD_5_SWITCHED_OFF(5, "Load 5 Switched Off"),
	LOAD_6_SWITCHED_OFF(6, "Load 6 Switched Off"),
	LOAD_7_SWITCHED_OFF(7, "Load 7 Switched Off"),
	LOAD_8_SWITCHED_OFF(8, "Load 8 Switched Off");
	
	private final int value;
	private final String name;
	
	private SwitchingState(int value, String name) {
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
