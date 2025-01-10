package io.openems.edge.controller.io.loadshedding;

import io.openems.common.types.OptionsEnum;

public enum LogicType implements OptionsEnum {
	
	/*
	 * Undefined type
	 */
	UNDEFINED(-1, "Undefined"),
	/*
	 * Positive logic - i.e switching output on drops load
	 */
	POSITIVE(0, "Positive"),
	/* 
	 * Negative logic - i.e output must be on for normal operation
	 */
	NEGATIVE(1, "Negative");
	
	private final int value;
	private final String name;
	
	private LogicType(int value, String name) {
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
		return LogicType.UNDEFINED;
	}
}
