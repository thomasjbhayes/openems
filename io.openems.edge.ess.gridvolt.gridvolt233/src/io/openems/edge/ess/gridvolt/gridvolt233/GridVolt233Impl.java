package io.openems.edge.ess.gridvolt.gridvolt233;


import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.DIRECT_1_TO_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_1;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_MINUS_3;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.channel.AccessMode;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.BitsWordElement;
import io.openems.edge.bridge.modbus.api.element.SignedWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.modbusslave.ModbusSlaveTable;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.ess.api.HybridEss;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.Power;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Ess.gridvolt.gridvolt233", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class GridVolt233Impl extends AbstractOpenemsModbusComponent 
		implements GridVolt233, ManagedSymmetricEss, SymmetricEss, ModbusComponent, 
		OpenemsComponent {

	@Reference
	private ConfigurationAdmin cm;
	
	private final Logger log = LoggerFactory.getLogger(GridVolt233Impl.class);

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	private Config config = null;

	public GridVolt233Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				GridVolt233.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsException {
		if(super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus",
				config.modbus_id())) {
			return;
		}
		this.config = config;
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {
		// TODO implement ModbusProtocol
		return new ModbusProtocol(this, //
				
				new FC3ReadRegistersTask(1046, Priority.LOW,
						m(new BitsWordElement(1046, this) // 
								.bit(0, GridVolt233.ChannelId.LEAKAGE_CURRENT_FAULT)
								.bit(1, GridVolt233.ChannelId.INSULATION_RESISTANCE_ABNORMALITY)
								.bit(2, GridVolt233.ChannelId.THYRISTOR_OVER_TEMPERATURE)
								.bit(3, GridVolt233.ChannelId.TRANSFORMER_OVER_TEMPERATURE)
								.bit(4, GridVolt233.ChannelId.POWER_SUPPLY_FAULT)
								.bit(5, GridVolt233.ChannelId.EXTERNAL_EPO)
								.bit(6, GridVolt233.ChannelId.EMS_FAULT)
								.bit(7, GridVolt233.ChannelId.PARALLEL_MACHINE_CABLE_FAULT)
								.bit(8, GridVolt233.ChannelId.PARALLEL_MACHINE_COMMUNICATION_FAULT)
								.bit(9, GridVolt233.ChannelId.GRID_OVERLOAD_FAULT)
								.bit(10, GridVolt233.ChannelId.BATTERY_FAULT)
								),
						m(new BitsWordElement(1047, this) // 
								.bit(0, GridVolt233.ChannelId.THYRISTOR_SHORT_CIRCUIT)
								.bit(1, GridVolt233.ChannelId.THYRISTOR_OPEN_CIRCUIT)
								.bit(2, GridVolt233.ChannelId.TRANSFORMER_PRE_CHARGING_FAULT)
								),
						m(new BitsWordElement(1048, this) // 
								.bit(0, GridVolt233.ChannelId.ABNORMAL_GRID_CURRENT_ZERO_OFFSET)
								.bit(1, GridVolt233.ChannelId.ABNORMAL_DC_CURRENT_ZERO_OFFSET)
								.bit(2, GridVolt233.ChannelId.ABNORMAL_LEAKAGE_CURRENT_ZERO_OFFSET)
								.bit(3, GridVolt233.ChannelId.REFERENCE_ABNORMALITY_2_5_VOLT)
								),
						m(new BitsWordElement(1049, this) // 
								.bit(0, GridVolt233.ChannelId.HIGH_AMBIENT_TEMPERATURE)
								.bit(1, GridVolt233.ChannelId.FAN_FAULT)
								.bit(2, GridVolt233.ChannelId.GRID_OVERLOAD_WARNING)
								.bit(3, GridVolt233.ChannelId.ABNORMAL_OIL_ENGINE_STARTUP)
								),
						m(new BitsWordElement(1050, this) // 
								.bit(0, GridVolt233.ChannelId.POSITIVE_BUSBAR_OVERVOLTAGE)
								.bit(1, GridVolt233.ChannelId.NEGATIVE_BUSBAR_OVERVOLTAGE)
								.bit(2, GridVolt233.ChannelId.DC_TOTAL_BUSBAR_OVERVOLTAGE)
								.bit(3, GridVolt233.ChannelId.BUSBAR_HALF_VOLTAGE_IMBALANCE)
								.bit(4, GridVolt233.ChannelId.DC_BUSBAR_SHORT_CIRCUIT)
								.bit(5, GridVolt233.ChannelId.DC_OVERCURRENT)
								.bit(6, GridVolt233.ChannelId.BALANCE_BRIDGE_OVERCURRENT)
								.bit(7, GridVolt233.ChannelId.DC_VOLTAGE_REVERSE_CONDITION)
								.bit(8, GridVolt233.ChannelId.LOW_DC_VOLTAGE)
								.bit(9, GridVolt233.ChannelId.HIGH_DC_VOLTAGE)
								.bit(10, GridVolt233.ChannelId.ABNORMAL_INSULATION_IMPEDANCE)
								.bit(11, GridVolt233.ChannelId.LOW_PHOTOVOLTAIC_POWER_SHUTDOWN)
								),
						m(new BitsWordElement(1051, this) // 
								.bit(0, GridVolt233.ChannelId.ABNORMAL_INVERTER_VOLTAGE_PHASE_A)
								.bit(1, GridVolt233.ChannelId.ABNORMAL_INVERTER_VOLTAGE_PHASE_B)
								.bit(2, GridVolt233.ChannelId.ABNORMAL_INVERTER_VOLTAGE_PHASE_C)
								.bit(3, GridVolt233.ChannelId.ABNORMAL_DC_VOLTAGE_IN_INVERTER_VOLTAGE_PHASE_A)
								.bit(4, GridVolt233.ChannelId.ABNORMAL_DC_VOLTAGE_IN_INVERTER_VOLTAGE_PHASE_B)
								.bit(5, GridVolt233.ChannelId.ABNORMAL_DC_VOLTAGE_IN_INVERTER_VOLTAGE_PHASE_C)
								.bit(6, GridVolt233.ChannelId.PHASE_A_OUTPUT_OVERLOAD_SHUTDOWN)
								.bit(7, GridVolt233.ChannelId.PHASE_B_OUTPUT_OVERLOAD_SHUTDOWN)
								.bit(8, GridVolt233.ChannelId.PHASE_C_OUTPUT_OVERLOAD_SHUTDOWN)
								.bit(9, GridVolt233.ChannelId.OUTPUT_OVERCURRENT_PHASE_A)
								.bit(10, GridVolt233.ChannelId.OUTPUT_OVERCURRENT_PHASE_B)
								.bit(11, GridVolt233.ChannelId.OUTPUT_OVERCURRENT_PHASE_C)
								.bit(12, GridVolt233.ChannelId.OUTPUT_SHORT_CIRCUIT_PHASE_A)
								.bit(13, GridVolt233.ChannelId.OUTPUT_SHORT_CIRCUIT_PHASE_B)
								.bit(14, GridVolt233.ChannelId.OUTPUT_SHORT_CIRCUIT_PHASE_C)
								.bit(15, GridVolt233.ChannelId.INVERTER_PHASE_NOT_SYNCHRONISED)
								),
						m(new BitsWordElement(1052, this) // 
								.bit(0, GridVolt233.ChannelId.GRID_OVER_VOLTAGE)
								.bit(1, GridVolt233.ChannelId.GRID_UNDER_VOLTAGE)
								.bit(2, GridVolt233.ChannelId.GRID_OVER_FREQUENCY)
								.bit(3, GridVolt233.ChannelId.GRID_UNDER_FREQUENCY)
								.bit(4, GridVolt233.ChannelId.ISLANDING_PROTECTION)
								.bit(5, GridVolt233.ChannelId.GRID_PHASE_MISMATCH)
								.bit(6, GridVolt233.ChannelId.AC_POWER_LOSS)
								.bit(7, GridVolt233.ChannelId.AC_WAVE_BY_WAVE_POWER_LIMITING_SHUTDOWN)
								.bit(8, GridVolt233.ChannelId.PARALLEL_CABLING_FAULT)
								.bit(9, GridVolt233.ChannelId.CARRIER_SYNCHONIZATION_FAULT)
								.bit(10, GridVolt233.ChannelId.INVERTER_SYNCHONIZATION_FAULT)
								.bit(11, GridVolt233.ChannelId.PARALLEL_COMMUNICATION_FAULT)
								.bit(12, GridVolt233.ChannelId.AC_FUSE_FAULT)
								.bit(13, GridVolt233.ChannelId.POWER_TRANSISTOR_OVER_TEMPERATURE)
								.bit(14, GridVolt233.ChannelId.POWER_SUPPLY_FAULT_)
								.bit(15, GridVolt233.ChannelId.LEAKAGE_CURRENT_FAULT_)
								),
						m(new BitsWordElement(1053, this) // 
								.bit(0, GridVolt233.ChannelId.DC_PRECHARGE_FAULT)
								.bit(1, GridVolt233.ChannelId.AC_PRECHARGE_FAULT)
								.bit(2, GridVolt233.ChannelId.DC_RELAY_SHORT_CIRCUIT)
								.bit(3, GridVolt233.ChannelId.DC_RELAY_OPEN_CIRCUIT)
								.bit(4, GridVolt233.ChannelId.AC_RELAY_SHORT_CIRCUIT_PHASE_A)
								.bit(5, GridVolt233.ChannelId.AC_RELAY_SHORT_CIRCUIT_PHASE_B)
								.bit(6, GridVolt233.ChannelId.AC_RELAY_SHORT_CIRCUIT_PHASE_C)
								.bit(7, GridVolt233.ChannelId.AC_RELAY_OPEN_CIRCUIT_PHASE_A)
								.bit(8, GridVolt233.ChannelId.AC_RELAY_OPEN_CIRCUIT_PHASE_B)
								.bit(9, GridVolt233.ChannelId.AC_RELAY_OPEN_CIRCUIT_PHASE_C)
								.bit(10, GridVolt233.ChannelId.BRIDGE_ARM_SHOOT_THROUGH_PHASE_A)
								.bit(11, GridVolt233.ChannelId.BRIDGE_ARM_SHOOT_THROUGH_PHASE_B)
								.bit(12, GridVolt233.ChannelId.BRIDGE_ARM_SHOOT_THROUGH_PHASE_C)
								),
						m(new BitsWordElement(1054, this) // 
								.bit(0, GridVolt233.ChannelId.GRID_CURRENT_ZERO_OFFSET_ABNORMALITY)
								.bit(1, GridVolt233.ChannelId.INVERTER_CURRENT_ZERO_OFFSET_ABNORMALITY)
								.bit(2, GridVolt233.ChannelId.INVERTER_CURRENT_DC_COMPONENT_ZERO_OFFSET_ABNORMALITY)
								.bit(3, GridVolt233.ChannelId.DC_CURRENT_ZERO_OFFSET_ABNORMALITY)
								.bit(4, GridVolt233.ChannelId.BALANCE_BRIDGE_CURRENT_ZERO_OFFSET_ABNORMALITY)
								.bit(5, GridVolt233.ChannelId.LEAKAGE_CURRENT_ZERO_OFFSET_ABNORMALITY)
								.bit(6, GridVolt233.ChannelId._2_5V_CURRENT_ZERO_OFFSET_ABNORMALITY)
								),
						m(new BitsWordElement(1055, this) // 
								.bit(0, GridVolt233.ChannelId.OUTPUT_OVERLOAD_ALARM_PHASE_A)
								.bit(1, GridVolt233.ChannelId.OUTPUT_OVERLOAD_ALARM_PHASE_B)
								.bit(2, GridVolt233.ChannelId.OUTPUT_OVERLOAD_ALARM_PHASE_C)
								.bit(3, GridVolt233.ChannelId.LOW_VOLTAGE_RIDE_THROUGH)
								.bit(4, GridVolt233.ChannelId.HIGH_VOLTAGE_RIDE_THROUGH)
								.bit(5, GridVolt233.ChannelId.BALANCE_BRIDGE_WAVE_BY_WAVE_CURRENT_LIMITING_ALARM)
								.bit(6, GridVolt233.ChannelId.BALANCE_BRIDGE_OVER_TEMPERATURE)
								.bit(7, GridVolt233.ChannelId._HIGH_AMBIENT_TEMPERATURE)
								.bit(8, GridVolt233.ChannelId.TEMPERATURE_DERATING)
								.bit(9, GridVolt233.ChannelId.DC_LIGHTNING_PROTECTION_FAULT)
								.bit(10, GridVolt233.ChannelId.AC_LIGHTNING_PROTECTION_FAULT)
								.bit(11, GridVolt233.ChannelId.FAN_FAULT_1)
								.bit(12, GridVolt233.ChannelId.FAN_FAULT_2)
								.bit(13, GridVolt233.ChannelId.BATTERY_OVERCHARGE)
								.bit(14, GridVolt233.ChannelId.BATTERY_OVERDISCHARGE)
								),
						m(new BitsWordElement(1056, this) // 
								.bit(0, GridVolt233.ChannelId.INTERNAL_COMMUNICATION_FAULT)
								)
						),
				new FC3ReadRegistersTask(1220, Priority.LOW, //
						m(new BitsWordElement(1220, this) // 
								.bit(0, GridVolt233.ChannelId.POSITIVE_HV_BUS_OVERVOLTAGE)
								.bit(1, GridVolt233.ChannelId.NEGATIVE_HV_BUS_OVERVOLTAGE)
								.bit(2, GridVolt233.ChannelId.HV_DC_BUS_OVERVOLTAGE)
								.bit(3, GridVolt233.ChannelId.HV_BUS_UNBALANCED_HALF_VOLTAGE)
								.bit(4, GridVolt233.ChannelId.HV_DC_BUS_SHORT_CIRCUIT)
								.bit(5, GridVolt233.ChannelId.HV_SIDE_OVERCURRENT)
								.bit(6, GridVolt233.ChannelId.HV_SIDE_LOW_EXTERNAL_VOLTAGE)
								.bit(7, GridVolt233.ChannelId.HV_SIDE_HIGH_EXTERNAL_VOLTAGE)
								.bit(8, GridVolt233.ChannelId.HV_SIDE_OVERLOAD)
								.bit(9, GridVolt233.ChannelId.GRADUAL_CURRENT_LIMITING_SHUTDOWN)
								.bit(10, GridVolt233.ChannelId.LOW_PHOTOVOLTAIC_POWER_SHUTDOWN_)
								),
						m(new BitsWordElement(1221, this) // 
								.bit(0, GridVolt233.ChannelId.EXTERNAL_VOLTAGE_REVERSE_CONNECTION_LV_SIDE_1)
								.bit(1, GridVolt233.ChannelId.LOW_EXTERNAL_VOLTAGE_LV_SIDE_1)
								.bit(2, GridVolt233.ChannelId.HIGH_EXTERNAL_VOLTAGE_LV_SIDE_1)
								.bit(3, GridVolt233.ChannelId.ABNORMAL_INSULATION_RESISTANCE_LV_SIDE_1)
								.bit(4, GridVolt233.ChannelId.INVERNAL_OVERVOLTAGE_LV_SIDE_1)
								.bit(5, GridVolt233.ChannelId.SHORT_CIRCUIT_LV_SIDE_1)
								.bit(6, GridVolt233.ChannelId.OVER_CURRENT_LV_SIDE_1)
								.bit(7, GridVolt233.ChannelId.OVERLOAD_LV_SIDE_1)
								.bit(8, GridVolt233.ChannelId.EXTERNAL_VOLTAGE_REVERSE_CONNECTION_LV_SIDE_2)
								.bit(9, GridVolt233.ChannelId.LOW_EXTERNAL_VOLTAGE_LV_SIDE_2)
								.bit(10, GridVolt233.ChannelId.HIGH_EXTERNAL_VOLTAGE_LV_SIDE_2)
								.bit(11, GridVolt233.ChannelId.ABNORMAL_INSULATION_RESISTANCE_LV_SIDE_2)
								.bit(12, GridVolt233.ChannelId.INVERNAL_OVERVOLTAGE_LV_SIDE_2)
								.bit(13, GridVolt233.ChannelId.SHORT_CIRCUIT_LV_SIDE_2)
								.bit(14, GridVolt233.ChannelId.OVER_CURRENT_LV_SIDE_2)
								.bit(15, GridVolt233.ChannelId.OVERLOAD_LV_SIDE_2)
								),
						m(new BitsWordElement(1222, this) // 
								.bit(5, GridVolt233.ChannelId.INCORRECT_BATTERY_TYPE_CONFIGURATION)
								)
						),
				// Status information
				new FC3ReadRegistersTask(1400, Priority.LOW, // 
						
						m(new BitsWordElement(1400, this) // 
								.bit(0, GridVolt233.ChannelId.BATTERY_NOT_UNDER_HIGH_VOLTAGE)
								.bit(1, GridVolt233.ChannelId.BATTERY_HIGH_VOLTAGE_POWER_UP_COMPLETED)
								.bit(2, GridVolt233.ChannelId.BATTERY_HIGH_VOLTAGE_POWER_UP_FAULT)
								.bit(3, GridVolt233.ChannelId.BATTERY_HIGH_VOLTAGE_POWER_DOWN_FAULT)
								.bit(4, GridVolt233.ChannelId.BATTERY_INITIALIZATION_STATUS)
								.bit(5, GridVolt233.ChannelId.BATTERY_NORMAL_STATUS)
								.bit(6, GridVolt233.ChannelId.BATTERY_FULLY_CHARGED_STATUS)
								.bit(7, GridVolt233.ChannelId.BATTERY_FULLY_DISCHARGED_STATUS)
								.bit(8, GridVolt233.ChannelId.BATTERY_FAULT_STATUS)),
						m(new BitsWordElement(1401, this) // 
								.bit(8, GridVolt233.ChannelId.ACCESS_CONTROL_STATUS)
								.bit(9, GridVolt233.ChannelId.EPO_STATUS)
								.bit(11, GridVolt233.ChannelId.SMOKE_DETECTION_STATUS)
								.bit(12, GridVolt233.ChannelId.LIGHTNING_PROTECTION)
								.bit(13, GridVolt233.ChannelId.FIRE_PROTECTION_ACTION)
								.bit(14, GridVolt233.ChannelId.PCS_ALARM)
								.bit(15, GridVolt233.ChannelId.PCS_FAULT)),
						m(new BitsWordElement(1402, this) // 
								.bit(4, GridVolt233.ChannelId.PHOTOVOLTAIC_SWITCH_1_STATUS)
								),
						m(new BitsWordElement(1403, this) // 
								.bit(0, GridVolt233.ChannelId.SYSTEM_STANDBY)
								.bit(1, GridVolt233.ChannelId.SYSTEM_ON)
								.bit(2, GridVolt233.ChannelId.SYSTEM_OFF)
								.bit(3, GridVolt233.ChannelId.SYSTEM_NOT_CHARGEABLE)
								.bit(4, GridVolt233.ChannelId.SYSTEM_NOT_DISCHARGEABLE)
								.bit(5, GridVolt233.ChannelId.BATTERY_NOT_STARTABLE)
								.bit(6, GridVolt233.ChannelId.SYSTEM_FAULT_TRIPPING)
								.bit(7, GridVolt233.ChannelId.ABNORMAL_BATTERY_STORAGE_TEMPERATURE_HUMIDITY)
								.bit(8, GridVolt233.ChannelId.ABNORMAL_BATTERY_OPERATION_TEMPERATURE_HUMIDITY)
								.bit(14, GridVolt233.ChannelId.ESS_GRID_CONNECTION_STATUS)
								)
						),
				new FC3ReadRegistersTask(1500, Priority.LOW,
						m(new BitsWordElement(1500, this) // 
								.bit(0, GridVolt233.ChannelId.AC_CONVERTER_1_DC_RELAY_STATUS)
								.bit(1, GridVolt233.ChannelId.AC_CONVERTER_1_AC_RELAY_STATUS)
								.bit(2, GridVolt233.ChannelId.AC_CONVERTER_1_CHARGE_DISCHARGE_STATUS)
								.bit(3, GridVolt233.ChannelId.AC_CONVERTER_1_OPERATING_STATUS)
								.bit(4, GridVolt233.ChannelId.AC_CONVERTER_1_FAULT_STATUS)
								)
						),
				new FC3ReadRegistersTask(1510, Priority.LOW,
						m(new BitsWordElement(1510, this) // 
								.bit(0, GridVolt233.ChannelId.AC_CONVERTER_2_DC_RELAY_STATUS)
								.bit(1, GridVolt233.ChannelId.AC_CONVERTER_2_AC_RELAY_STATUS)
								.bit(2, GridVolt233.ChannelId.AC_CONVERTER_2_CHARGE_DISCHARGE_STATUS)
								.bit(3, GridVolt233.ChannelId.AC_CONVERTER_2_OPERATING_STATUS)
								.bit(4, GridVolt233.ChannelId.AC_CONVERTER_2_FAULT_STATUS)
								)
						),
				new FC3ReadRegistersTask(1670, Priority.LOW,
						m(new BitsWordElement(1510, this) // 
								.bit(0, GridVolt233.ChannelId.DC_CONVERTER_1_LOW_VOLTAGE_SIDE_1_PRECHARGING)
								.bit(1, GridVolt233.ChannelId.DC_CONVERTER_1_LOW_VOLTAGE_SIDE_2_PRECHARGING)
								.bit(2, GridVolt233.ChannelId.DC_CONVERTER_1_LOW_VOLTAGE_SIDE_1_RELAY)
								.bit(3, GridVolt233.ChannelId.DC_CONVERTER_1_LOW_VOLTAGE_SIDE_2_RELAY)
								.bit(4, GridVolt233.ChannelId.DC_CONVERTER_1_HIGH_VOLTAGE_SIDE_PRECHARGING)
								.bit(5, GridVolt233.ChannelId.DC_CONVERTER_1_HIGH_VOLTAGE_SIDE_RELAY)
								.bit(6, GridVolt233.ChannelId.DC_CONVERTER_1_OPERATING_STATUS)
								.bit(7, GridVolt233.ChannelId.DC_CONVERTER_1_FAULT_STATUS)
								)
						),
				new FC3ReadRegistersTask(1680, Priority.LOW,
						m(new BitsWordElement(1510, this) // 
								.bit(0, GridVolt233.ChannelId.DC_CONVERTER_2_LOW_VOLTAGE_SIDE_1_PRECHARGING)
								.bit(1, GridVolt233.ChannelId.DC_CONVERTER_2_LOW_VOLTAGE_SIDE_2_PRECHARGING)
								.bit(2, GridVolt233.ChannelId.DC_CONVERTER_2_LOW_VOLTAGE_SIDE_1_RELAY)
								.bit(3, GridVolt233.ChannelId.DC_CONVERTER_2_LOW_VOLTAGE_SIDE_2_RELAY)
								.bit(4, GridVolt233.ChannelId.DC_CONVERTER_2_HIGH_VOLTAGE_SIDE_PRECHARGING)
								.bit(5, GridVolt233.ChannelId.DC_CONVERTER_2_HIGH_VOLTAGE_SIDE_RELAY)
								.bit(6, GridVolt233.ChannelId.DC_CONVERTER_2_OPERATING_STATUS)
								.bit(7, GridVolt233.ChannelId.DC_CONVERTER_2_FAULT_STATUS)
								)
						),
				new FC3ReadRegistersTask(1918, Priority.LOW,
						m(SymmetricEss.ChannelId.MAX_CELL_TEMPERATURE, new SignedWordElement(1918), DIRECT_1_TO_1),
						m(SymmetricEss.ChannelId.MIN_CELL_TEMPERATURE, new SignedWordElement(1919), DIRECT_1_TO_1),
						m(SymmetricEss.ChannelId.MAX_CELL_VOLTAGE, new SignedWordElement(1920), SCALE_FACTOR_MINUS_3),
						m(SymmetricEss.ChannelId.MIN_CELL_VOLTAGE, new SignedWordElement(1921), SCALE_FACTOR_MINUS_3),
						m(GridVolt233.ChannelId.BATTERY_MAXIMUM_DISCHARGE_CURRENT, new SignedWordElement(1922), SCALE_FACTOR_MINUS_1),
						m(GridVolt233.ChannelId.BATTERY_MAXIMUM_CHARGE_CURRENT, new SignedWordElement(1923), SCALE_FACTOR_MINUS_1),
						m(SymmetricEss.ChannelId.SOC, new SignedWordElement(1924), SCALE_FACTOR_MINUS_1),
						m(GridVolt233.ChannelId.SOH, new SignedWordElement(1925), DIRECT_1_TO_1)),
				new FC3ReadRegistersTask(1918, Priority.LOW,
						m(GridVolt233.ChannelId.DC_MODULE_BATTERY_VOLTAGE, new SignedWordElement(1928), SCALE_FACTOR_MINUS_1),
						m(GridVolt233.ChannelId.DC_MODULE_BATTERY_CURRENT, new SignedWordElement(1929), SCALE_FACTOR_MINUS_1),
						m(HybridEss.ChannelId.DC_DISCHARGE_POWER, new SignedWordElement(1930), SCALE_FACTOR_2),
						m(GridVolt233.ChannelId.DC_MODULE_PHOTOVOLTAIC_VOLTAGE, new SignedWordElement(1931), SCALE_FACTOR_MINUS_1),
						m(GridVolt233.ChannelId.DC_MODULE_PHOTOVOLTAIC_CURRENT, new SignedWordElement(1932), SCALE_FACTOR_MINUS_1),
						m(GridVolt233.ChannelId.DC_MODULE_PHOTOVOLTAIC_POWER, new SignedWordElement(1933), SCALE_FACTOR_2)
						));
	}

	@Override
	public String debugLog() {
		return "Hello World";
	}

	@Override
	public Power getPower() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void applyPower(int activePower, int reactivePower) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPowerPrecision() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ModbusSlaveTable getModbusSlaveTable(AccessMode accessMode) {
		// TODO Auto-generated method stub
		return null;
	}
}
