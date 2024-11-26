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

		// 1048
		ABNORMAL_GRID_CURRENT_ZERO_OFFSET(Doc.of(Level.WARNING)),
		ABNORMAL_DC_CURRENT_ZERO_OFFSET(Doc.of(Level.WARNING)),
		ABNORMAL_LEAKAGE_CURRENT_ZERO_OFFSET(Doc.of(Level.WARNING)),
		REFERENCE_ABNORMALITY_2_5_VOLT(Doc.of(Level.WARNING).text("2.5V Reference abnormality")),
		
		// 1049
		HIGH_AMBIENT_TEMPERATURE(Doc.of(Level.WARNING).text("High ambient temperature")),
		FAN_FAULT(Doc.of(Level.FAULT).text("Fan fault")),
		GRID_OVERLOAD_WARNING(Doc.of(Level.WARNING).text("Grid overload warning")),
		ABNORMAL_OIL_ENGINE_STARTUP(Doc.of(Level.WARNING).text("Abnormal oil engine startup")),
		
		// 1050
		POSITIVE_BUSBAR_OVERVOLTAGE(Doc.of(Level.WARNING).text("Positive busbar over-voltage")),
		NEGATIVE_BUSBAR_OVERVOLTAGE(Doc.of(Level.WARNING).text("Negative busbar over-voltage")),
		DC_TOTAL_BUSBAR_OVERVOLTAGE(Doc.of(Level.WARNING).text("DC total busbar overvoltage")),
		BUSBAR_HALF_VOLTAGE_IMBALANCE(Doc.of(Level.WARNING).text("Busbar half-voltage imbalance")),
		DC_BUSBAR_SHORT_CIRCUIT(Doc.of(Level.WARNING).text("DC Busbar short circuit")),
		DC_OVERCURRENT(Doc.of(Level.WARNING).text("DC Overcurrent")),
		BALANCE_BRIDGE_OVERCURRENT(Doc.of(Level.WARNING).text("Balance bridge overcurrent")),
		DC_VOLTAGE_REVERSE_CONDITION(Doc.of(Level.WARNING).text("DC Voltage reverse condition")),
		LOW_DC_VOLTAGE(Doc.of(Level.WARNING).text("Low DC Voltage")),
		HIGH_DC_VOLTAGE(Doc.of(Level.WARNING).text("High DC Voltage")),
		ABNORMAL_INSULATION_IMPEDANCE(Doc.of(Level.WARNING).text("Abnormal insulation impedance")),
		LOW_PHOTOVOLTAIC_POWER_SHUTDOWN(Doc.of(Level.WARNING).text("Low photovoltaic power shutdown")),
		
		// 1051
		ABNORMAL_INVERTER_VOLTAGE_PHASE_A(Doc.of(Level.WARNING).text("Abnormal inverter voltage phase A")),
		ABNORMAL_INVERTER_VOLTAGE_PHASE_B(Doc.of(Level.WARNING).text("Abnormal inverter voltage phase B")),
		ABNORMAL_INVERTER_VOLTAGE_PHASE_C(Doc.of(Level.WARNING).text("Abnormal inverter voltage phase C")),
		ABNORMAL_DC_VOLTAGE_IN_INVERTER_VOLTAGE_PHASE_A(Doc.of(Level.WARNING).text("Abnormal DC voltage in inverter phase A")),
		ABNORMAL_DC_VOLTAGE_IN_INVERTER_VOLTAGE_PHASE_B(Doc.of(Level.WARNING).text("Abnormal DC voltage in inverter phase B")),
		ABNORMAL_DC_VOLTAGE_IN_INVERTER_VOLTAGE_PHASE_C(Doc.of(Level.WARNING).text("Abnormal DC voltage in inverter phase C")),
		PHASE_A_OUTPUT_OVERLOAD_SHUTDOWN(Doc.of(Level.WARNING).text("Phase A output overload shutdown")),
		PHASE_B_OUTPUT_OVERLOAD_SHUTDOWN(Doc.of(Level.WARNING).text("Phase B output overload shutdown")),
		PHASE_C_OUTPUT_OVERLOAD_SHUTDOWN(Doc.of(Level.WARNING).text("Phase C output overload shutdown")),
		OUTPUT_OVERCURRENT_PHASE_A(Doc.of(Level.WARNING).text("Output overcurrent phase A")),
		OUTPUT_OVERCURRENT_PHASE_B(Doc.of(Level.WARNING).text("Output overcurrent phase B")),
		OUTPUT_OVERCURRENT_PHASE_C(Doc.of(Level.WARNING).text("Output overcurrent phase C")),
		OUTPUT_SHORT_CIRCUIT_PHASE_A(Doc.of(Level.WARNING).text("Output short circuit phase A")),
		OUTPUT_SHORT_CIRCUIT_PHASE_B(Doc.of(Level.WARNING).text("Output short circuit phase B")),
		OUTPUT_SHORT_CIRCUIT_PHASE_C(Doc.of(Level.WARNING).text("Output short circuit phase C")),
		INVERTER_PHASE_NOT_SYNCHRONISED(Doc.of(Level.WARNING).text("Inverter phase not synchronized")),
		
		// 1052
		GRID_OVER_VOLTAGE(Doc.of(Level.WARNING).text("Grid over-voltage")),
		GRID_UNDER_VOLTAGE(Doc.of(Level.WARNING).text("Grid under-voltage")),
		GRID_OVER_FREQUENCY(Doc.of(Level.WARNING).text("Grid over-frequency")),
		GRID_UNDER_FREQUENCY(Doc.of(Level.WARNING).text("Grid under-frequency")),
		ISLANDING_PROTECTION(Doc.of(Level.WARNING).text("Islanding protection")),
		GRID_PHASE_MISMATCH(Doc.of(Level.WARNING).text("Grid phase mismatch")),
		AC_POWER_LOSS(Doc.of(Level.WARNING).text("AC Power Loss")),
		AC_WAVE_BY_WAVE_POWER_LIMITING_SHUTDOWN(Doc.of(Level.WARNING).text("AC Wave-by-wave power limiting shutdown")),
		PARALLEL_CABLING_FAULT(Doc.of(Level.WARNING).text("Parallel cabling fault")),
		CARRIER_SYNCHRONIZATION_FAULT(Doc.of(Level.WARNING).text("Carrier synchronization fault")),
		INVERTER_SYNCHONIZATION_FAULT(Doc.of(Level.WARNING).text("Inverter synchonization fault")),
		PARALLEL_COMMUNICATION_FAULT(Doc.of(Level.WARNING).text("Parallel communication fault")),
		AC_FUSE_FAULT(Doc.of(Level.WARNING).text("AC fuse fault")),
		POWER_TRANSISTOR_OVER_TEMPERATURE(Doc.of(Level.WARNING).text("Power transistor over temperature")),
		POWER_SUPPLY_FAULT_(Doc.of(Level.WARNING).text("Power supply fault")),
		LEAKAGE_CURRENT_FAULT_(Doc.of(Level.WARNING).text("Leakage current fault")),
		
		// 1053
		DC_PRECHARGE_FAULT(Doc.of(Level.WARNING).text("DC Precharge fault")),
		AC_PRECHARGE_FAULT(Doc.of(Level.WARNING).text("AC Precharge fault")),
		DC_RELAY_SHORT_CIRCUIT(Doc.of(Level.WARNING).text("DC relay short circuit")),
		DC_RELAY_OPEN_CIRCUIT(Doc.of(Level.WARNING).text("DC relay open circuit")),
		AC_RELAY_SHORT_CIRCUIT_PHASE_A(Doc.of(Level.WARNING).text("AC relay short circuit phase A")),
		AC_RELAY_SHORT_CIRCUIT_PHASE_B(Doc.of(Level.WARNING).text("AC relay short circuit phase B")),
		AC_RELAY_SHORT_CIRCUIT_PHASE_C(Doc.of(Level.WARNING).text("AC relay short circuit phase C")),
		AC_RELAY_OPEN_CIRCUIT_PHASE_A(Doc.of(Level.WARNING).text("AC relay open circuit phase A")),
		AC_RELAY_OPEN_CIRCUIT_PHASE_B(Doc.of(Level.WARNING).text("AC relay open circuit phase B")),
		AC_RELAY_OPEN_CIRCUIT_PHASE_C(Doc.of(Level.WARNING).text("AC relay open circuit phase C")),
		BRIDGE_ARM_SHOOT_THROUGH_PHASE_A(Doc.of(Level.WARNING).text("Bridge arm shoot through phase A")),
		BRIDGE_ARM_SHOOT_THROUGH_PHASE_B(Doc.of(Level.WARNING).text("Bridge arm shoot through phase B")),
		BRIDGE_ARM_SHOOT_THROUGH_PHASE_C(Doc.of(Level.WARNING).text("Bridge arm shoot through phase C")),
		
		// 1054
		GRID_CURRENT_ZERO_OFFSET_ABNORMALITY(Doc.of(Level.WARNING).text("Grid current zero offset abnormality")),
		INVERTER_CURRENT_ZERO_OFFSET_ABNORMALITY(Doc.of(Level.WARNING).text("Inverter current zero offset abnormality")),
		INVERTER_CURRENT_DC_COMPONENT_ZERO_OFFSET_ABNORMALITY(Doc.of(Level.WARNING).text("Inverter current DC component zero offset abnormality")),
		DC_CURRENT_ZERO_OFFSET_ABNORMALITY(Doc.of(Level.WARNING).text("DC current zero offset abnormality")),
		BALANCE_BRIDGE_CURRENT_ZERO_OFFSET_ABNORMALITY(Doc.of(Level.WARNING).text("Balance bridge current zero offset abnormality")),
		LEAKAGE_CURRENT_ZERO_OFFSET_ABNORMALITY(Doc.of(Level.WARNING).text("Leakage current zero offset abnormality")),
		_2_5V_CURRENT_ZERO_OFFSET_ABNORMALITY(Doc.of(Level.WARNING).text("2.5V current zero offset abnormality")),
		
		// 1055
		OUTPUT_OVERLOAD_ALARM_PHASE_A(Doc.of(Level.WARNING).text("Output overload alarm Phase A")),
		OUTPUT_OVERLOAD_ALARM_PHASE_B(Doc.of(Level.WARNING).text("Output overload alarm Phase B")),
		OUTPUT_OVERLOAD_ALARM_PHASE_C(Doc.of(Level.WARNING).text("Output overload alarm Phase C")),
		LOW_VOLTAGE_RIDE_THROUGH(Doc.of(Level.WARNING).text("Low voltage ride through")),
		HIGH_VOLTAGE_RIDE_THROUGH(Doc.of(Level.WARNING).text("High voltage ride through")),
		BALANCE_BRIDGE_WAVE_BY_WAVE_CURRENT_LIMITING_ALARM(Doc.of(Level.WARNING).text("Balance bridge wave-by-wave current limiting alarm")),
		BALANCE_BRIDGE_OVER_TEMPERATURE(Doc.of(Level.WARNING).text("Balance bridge over temperature")),
		_HIGH_AMBIENT_TEMPERATURE(Doc.of(Level.WARNING).text("High ambient temperature")),
		TEMPERATURE_DERATING(Doc.of(Level.WARNING).text("Temperature derating")),
		DC_LIGHTNING_PROTECTION_FAULT(Doc.of(Level.WARNING).text("DC Lightning protection fault")),
		AC_LIGHTNING_PROTECTION_FAULT(Doc.of(Level.WARNING).text("AC Lightning protection fault")),
		FAN_FAULT_1(Doc.of(Level.WARNING).text("Fan fault 1")),
		FAN_FAULT_2(Doc.of(Level.WARNING).text("Fan fault 2")),
		BATTERY_OVERCHARGE(Doc.of(Level.WARNING).text("Battery overcharge")),
		BATTERY_OVERDISCHARGE(Doc.of(Level.WARNING).text("Battery over-discharge")),
		
		// 1056
		INTERNAL_COMMUNICATION_FAULT(Doc.of(Level.FAULT).text("Internal communication fault")),
		
		// 1220
		POSITIVE_HV_BUS_OVERVOLTAGE(Doc.of(Level.FAULT).text("Positive HV bus overvoltage")),
		NEGATIVE_HV_BUS_OVERVOLTAGE(Doc.of(Level.FAULT).text("Negative HV bus overvoltage")),
		HV_DC_BUS_OVERVOLTAGE(Doc.of(Level.FAULT).text("HV DC Bus overvoltage")),
		HV_BUS_UNBALANCED_HALF_VOLTAGE(Doc.of(Level.FAULT).text("HV bus unbalanced half voltage")),
		HV_DC_BUS_SHORT_CIRCUIT(Doc.of(Level.FAULT).text("HV DC Bus short circuit")),
		HV_SIDE_OVERCURRENT(Doc.of(Level.FAULT).text("HV side overcurrent")),
		HV_SIDE_LOW_EXTERNAL_VOLTAGE(Doc.of(Level.FAULT).text("HV side low external voltage")),
		HV_SIDE_HIGH_EXTERNAL_VOLTAGE(Doc.of(Level.FAULT).text("HV side high external voltage")),
		HV_SIDE_OVERLOAD(Doc.of(Level.FAULT).text("HV side overload")),
		GRADUAL_CURRENT_LIMITING_SHUTDOWN(Doc.of(Level.FAULT).text("Gradual current limiting shutdown")),
		LOW_PHOTOVOLTAIC_POWER_SHUTDOWN_(Doc.of(Level.FAULT).text("Low photovoltaic power shutdown")),
		
		// 1221
		EXTERNAL_VOLTAGE_REVERSE_CONNECTION_LV_SIDE_1(Doc.of(Level.WARNING).text("External voltage reverse connection LV side 1")),
		LOW_EXTERNAL_VOLTAGE_LV_SIDE_1(Doc.of(Level.WARNING).text("Low external voltage LV side 1")),
		HIGH_EXTERNAL_VOLTAGE_LV_SIDE_1(Doc.of(Level.WARNING).text("High external voltage LV side 1")),
		ABNORMAL_INSULATION_RESISTANCE_LV_SIDE_1(Doc.of(Level.WARNING).text("High external voltage LV side 1")),
		INVERNAL_OVERVOLTAGE_LV_SIDE_1(Doc.of(Level.WARNING).text("Abnormal insulation resistance LV side 1")),
		SHORT_CIRCUIT_LV_SIDE_1(Doc.of(Level.WARNING).text("Short circuit LV side 1")),
		OVER_CURRENT_LV_SIDE_1(Doc.of(Level.WARNING).text("Over current LV side 1")),
		OVERLOAD_LV_SIDE_1(Doc.of(Level.WARNING).text("Overload LV side 1")),
		EXTERNAL_VOLTAGE_REVERSE_CONNECTION_LV_SIDE_2(Doc.of(Level.WARNING).text("External voltage reverse connection LV side 2")),
		LOW_EXTERNAL_VOLTAGE_LV_SIDE_2(Doc.of(Level.WARNING).text("Low external voltage LV side 2")),
		HIGH_EXTERNAL_VOLTAGE_LV_SIDE_2(Doc.of(Level.WARNING).text("High external voltage LV side 2")),
		ABNORMAL_INSULATION_RESISTANCE_LV_SIDE_2(Doc.of(Level.WARNING).text("High external voltage LV side 2")),
		INVERNAL_OVERVOLTAGE_LV_SIDE_2(Doc.of(Level.WARNING).text("Abnormal insulation resistance LV side 2")),
		SHORT_CIRCUIT_LV_SIDE_2(Doc.of(Level.WARNING).text("Short circuit LV side 2")),
		OVER_CURRENT_LV_SIDE_2(Doc.of(Level.WARNING).text("Over current LV side 2")),
		OVERLOAD_LV_SIDE_2(Doc.of(Level.WARNING).text("Overload LV side 2")),
		
		// 1222
		INCORRECT_BATTERY_TYPE_CONFIGURATION(Doc.of(Level.WARNING).text("Incorrect battery type configuration")),
		
		
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
