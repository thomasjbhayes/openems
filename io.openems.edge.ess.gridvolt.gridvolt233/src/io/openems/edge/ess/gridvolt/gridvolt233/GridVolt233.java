package io.openems.edge.ess.gridvolt.gridvolt233;

import org.osgi.service.event.EventHandler;

import io.openems.common.channel.Level;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.BooleanDoc;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlave;
import io.openems.edge.common.sum.GridMode;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;

public interface GridVolt233 
		extends ManagedSymmetricEss, SymmetricEss, OpenemsComponent, ModbusSlave {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
		
		// Monet model
		INVERTER_MODEL(Doc.of(OpenemsType.STRING)),
		INVERTER_MANUFACTURER_NAME(Doc.of(OpenemsType.STRING)),
		MONITORING_VERSION(Doc.of(OpenemsType.STRING)),
		EMS_SERIAL_NUMBER(Doc.of(OpenemsType.STRING)),
		
		// 1400 System status information
		BATTERY_NOT_UNDER_HIGH_VOLTAGE(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery not under high voltage")), //
		BATTERY_HIGH_VOLTAGE_POWER_UP_COMPLETED(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery high voltage power up completed")), //
		BATTERY_HIGH_VOLTAGE_POWER_UP_FAULT(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery high voltage power up fault")), //
		BATTERY_HIGH_VOLTAGE_POWER_DOWN_FAULT(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery high voltage power down fault")), //
		BATTERY_INITIALIZATION_STATUS(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery initialization status")), //
		BATTERY_NORMAL_STATUS(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery normal status")), //
		BATTERY_FULLY_CHARGED_STATUS(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery fully charged status")), //
		BATTERY_FULLY_DISCHARGED_STATUS(Doc.of(OpenemsType.BOOLEAN) //
				.text("Battery fully discharged status")), //
		BATTERY_FAULT_STATUS(Doc.of(Level.FAULT) //
				.text("Battery fault status")), //
		
		// 1401 
		ACCESS_CONTROL_STATUS(Doc.of(OpenemsType.BOOLEAN) //
				.text("Access control status (0: Off, 1: On)")), //
		EPO_STATUS(Doc.of(OpenemsType.BOOLEAN) //
				.text("EPO (0: Normal, 1: Malfunction)")), //
		SMOKE_DETECTION_STATUS(Doc.of(Level.FAULT) //
				.text("Smoke detection status (0: Normal, 1: Malfunction)")), //
		LIGHTNING_PROTECTION(Doc.of(Level.WARNING) //
				.text("Lightning protection (0: Normal, 1: Malfunction)")), //
		FIRE_PROTECTION_ACTION(Doc.of(Level.FAULT) //
				.text("Smoke detection status (0: Normal, 1: Malfunction)")), //
		PCS_ALARM(Doc.of(OpenemsType.BOOLEAN) //
				.text("PCS Alarm (0: Normal, 1: Alarm)")), //
		PCS_FAULT(Doc.of(OpenemsType.BOOLEAN) //
				.text("PCS Alarm (0: Normal, 1: Malfunction)")), //
		
		
		// 1402
		PHOTOVOLTAIC_SWITCH_1_STATUS(Doc.of(OpenemsType.BOOLEAN) // 
				.text("Photovoltaic switch 1 status. (0: Disconnected 1: Connected")),
		
		// 1403
		SYSTEM_STANDBY(Doc.of(OpenemsType.BOOLEAN).text("System standby (0: No, 1: Yes)")),
		SYSTEM_ON(Doc.of(OpenemsType.BOOLEAN).text("System on (0: No, 1: Yes)")), 
		SYSTEM_OFF(Doc.of(OpenemsType.BOOLEAN).text("System off (0: No, 1: Yes)")), 
		SYSTEM_NOT_CHARGEABLE(Doc.of(OpenemsType.BOOLEAN).text("System not chargeable (0: No, 1: Yes)")), 
		SYSTEM_NOT_DISCHARGEABLE(Doc.of(OpenemsType.BOOLEAN).text("System not dischargeable (0: No, 1: Yes)")), 
		BATTERY_NOT_STARTABLE(Doc.of(OpenemsType.BOOLEAN).text("System not startable (0: No, 1: Yes)")), 
		SYSTEM_FAULT_TRIPPING(Doc.of(OpenemsType.BOOLEAN).text("System fault tripping - excluding PCS faults (0: No, 1: Yes)")), 
		ABNORMAL_BATTERY_STORAGE_TEMPERATURE_HUMIDITY(Doc.of(OpenemsType.BOOLEAN).text("Abnormal battery temperature or humidity for battery storage (0: No, 1: Yes)")),
		ABNORMAL_BATTERY_OPERATION_TEMPERATURE_HUMIDITY(Doc.of(OpenemsType.BOOLEAN).text("Abnormal battery temperature or humidity for battery operation (0: No, 1: Yes)")), 
		
		ESS_GRID_CONNECTION_STATUS(new BooleanDoc() // Maps the custom GridMode variable to the OpenEMS GridMode
				.<GridVolt233>onChannelChange((self, value) -> {
					final GridMode gridMode; // This implementation is based on the implementation in BatteryInverterSinexcel
					if (!value.isDefined()) { // TODO: Test that this works
						gridMode = GridMode.UNDEFINED;
					} else if (value.get()) {
						gridMode = GridMode.OFF_GRID;
					} else {
						gridMode = GridMode.ON_GRID;
					}
					self._setGridMode(gridMode);
				}).text("0: Grid connected, 1: Off-grid")),
		
		AUTOMATIC_MODE_STATUS(Doc.of(OpenemsType.BOOLEAN).text("Automatic mode status. 0: Stop, 1: Run")),
		
		// 1500 AC Converter Module 1 Status
		AC_CONVERTER_1_DC_RELAY_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 1 - DC Relay Status (0: Off, 1: On)")),
		AC_CONVERTER_1_AC_RELAY_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 1 - AC Relay Status (0: Off, 1: On)")),
		AC_CONVERTER_1_CHARGE_DISCHARGE_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 1 - Charge/Discharge Status (0: Discharging, 1: Charging)")),
		AC_CONVERTER_1_OPERATING_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 1 - Charge/Discharge Status (0: Stop, 1: Run)")),
		AC_CONVERTER_1_FAULT_STATUS(Doc.of(Level.FAULT).text("AC Module 1 - Fault Status (0: Normal, 1: Malfunction)")),
		
		// 1510 AC Converter Module 2 Status
		AC_CONVERTER_2_DC_RELAY_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 2 - DC Relay Status (0: Off, 1: On)")),
		AC_CONVERTER_2_AC_RELAY_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 2 - AC Relay Status (0: Off, 1: On)")),
		AC_CONVERTER_2_CHARGE_DISCHARGE_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 2 - Charge/Discharge Status (0: Discharging, 1: Charging)")),
		AC_CONVERTER_2_OPERATING_STATUS(Doc.of(OpenemsType.BOOLEAN).text("AC Module 2 - Charge/Discharge Status (0: Stop, 1: Run)")),
		AC_CONVERTER_2_FAULT_STATUS(Doc.of(Level.FAULT).text("AC Module 2 - Fault Status (0: Normal, 1: Malfunction)")),
		
		// 1670 DC Converter Module 1 Status
		DC_CONVERTER_1_LOW_VOLTAGE_SIDE_1_PRECHARGING(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 1: Low-voltage side 1 pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_1_LOW_VOLTAGE_SIDE_2_PRECHARGING(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 1: Low-voltage side 2 pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_1_LOW_VOLTAGE_SIDE_1_RELAY(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 1: Low-voltage side 1 relay (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_1_LOW_VOLTAGE_SIDE_2_RELAY(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 1: Low-voltage side 2 pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_1_HIGH_VOLTAGE_SIDE_PRECHARGING(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 1: High-voltage side pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_1_HIGH_VOLTAGE_SIDE_RELAY(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 1: High-voltage side relay (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_1_OPERATING_STATUS(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 1: Operating status (0: Stop, 1: Run)")),
		DC_CONVERTER_1_FAULT_STATUS(Doc.of(Level.FAULT).text("DC Converter 1: Fault status (0: Normal, 1: Fault)")),
		
		
		// 1680 DC Converter Module 2 Status
		DC_CONVERTER_2_LOW_VOLTAGE_SIDE_1_PRECHARGING(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 2: Low-voltage side 1 pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_2_LOW_VOLTAGE_SIDE_2_PRECHARGING(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 2: Low-voltage side 2 pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_2_LOW_VOLTAGE_SIDE_1_RELAY(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 2: Low-voltage side 1 relay (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_2_LOW_VOLTAGE_SIDE_2_RELAY(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 2: Low-voltage side 2 pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_2_HIGH_VOLTAGE_SIDE_PRECHARGING(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 2: High-voltage side pre-charging (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_2_HIGH_VOLTAGE_SIDE_RELAY(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 2: High-voltage side relay (0: Disconnected, 1: Connected)")),
		DC_CONVERTER_2_OPERATING_STATUS(Doc.of(OpenemsType.BOOLEAN).text("DC Converter 2: Operating status (0: Stop, 1: Run)")),
		DC_CONVERTER_2_FAULT_STATUS(Doc.of(Level.FAULT).text("DC Converter 2: Fault status (0: Normal, 1: Fault)")),
		
		// Fault alarm information
		
		// 1046
		LEAKAGE_CURRENT_FAULT(Doc.of(Level.FAULT).text("Leagage current fault")),
		INSULATION_RESISTANCE_ABNORMALITY(Doc.of(Level.FAULT).text("Insulation resistance abnormality")),
		THYRISTOR_OVER_TEMPERATURE(Doc.of(Level.FAULT).text("Thyristor over-temperature")),
		TRANSFORMER_OVER_TEMPERATURE(Doc.of(Level.FAULT).text("Transformer over-temperature")),
		POWER_SUPPLY_FAULT(Doc.of(Level.FAULT).text("Power supply fault")),
		EXTERNAL_EPO(Doc.of(Level.FAULT).text("External EPO")),
		EMS_FAULT(Doc.of(Level.FAULT).text("EMS Fault")),
		PARALLEL_MACHINE_CABLE_FAULT(Doc.of(Level.FAULT).text("Parallel machine cable fault")),
		PARALLEL_MACHINE_COMMUNICATION_FAULT(Doc.of(Level.FAULT).text("Parallel machine communication fault")),
		GRID_OVERLOAD_FAULT(Doc.of(Level.FAULT).text("Grid overload fault")),
		BATTERY_FAULT(Doc.of(Level.FAULT).text("Battery fault")),
		
		// 1047
		THYRISTOR_SHORT_CIRCUIT(Doc.of(Level.WARNING).text("Thyristor short circuit")),
		THYRISTOR_OPEN_CIRCUIT(Doc.of(Level.WARNING).text("Thyristor open circuit")),
		TRANSFORMER_PRE_CHARGING_FAULT(Doc.of(Level.FAULT).text("Transformer pre-charging fault")),

		
		// 9.5.1 System statistics
		BATTERY_MAXIMUM_DISCHARGE_CURRENT(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.AMPERE)),
		BATTERY_MAXIMUM_CHARGE_CURRENT(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.AMPERE)),
		SOH(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.PERCENT)
				.text("State of Health (SOH)")),
		DC_MODULE_BATTERY_VOLTAGE(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.VOLT)),
		DC_MODULE_BATTERY_CURRENT(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.AMPERE)),
		DC_MODULE_PHOTOVOLTAIC_VOLTAGE(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.VOLT)),
		DC_MODULE_PHOTOVOLTAIC_CURRENT(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.AMPERE)),
		DC_MODULE_PHOTOVOLTAIC_POWER(Doc.of(OpenemsType.INTEGER)
				.unit(Unit.WATT)),
		
		
		// 9.6 System setting parameters
		
		;
		
		

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

}
