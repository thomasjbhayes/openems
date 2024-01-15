package io.openems.edge.ess.gridvolt.gridvolt1.statemachine;

import io.openems.edge.battery.api.Battery;
import io.openems.edge.batteryinverter.api.ManagedSymmetricBatteryInverter;
import io.openems.edge.common.statemachine.AbstractContext;
import io.openems.edge.ess.gridvolt.gridvolt1.GridVolt1;

public class Context extends AbstractContext<GridVolt1> {
	protected final Battery battery;
	protected final ManagedSymmetricBatteryInverter batteryInverter;
	
	public Context(GridVolt1 parent, Battery battery, ManagedSymmetricBatteryInverter batteryInverter) {
		super(parent);
		this.battery = battery;
		this.batteryInverter = batteryInverter;
	}
	
	public Battery getBattery() {
		return this.battery;
	}

}
