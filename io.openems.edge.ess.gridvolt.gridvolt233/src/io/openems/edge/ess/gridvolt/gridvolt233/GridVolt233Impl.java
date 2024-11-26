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
						m(new BitsWordElement(1046, this) // 
								.bit(0, GridVolt233.ChannelId.THYRISTOR_SHORT_CIRCUIT)
								.bit(1, GridVolt233.ChannelId.THYRISTOR_OPEN_CIRCUIT)
								.bit(2, GridVolt233.ChannelId.TRANSFORMER_PRE_CHARGING_FAULT)
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
