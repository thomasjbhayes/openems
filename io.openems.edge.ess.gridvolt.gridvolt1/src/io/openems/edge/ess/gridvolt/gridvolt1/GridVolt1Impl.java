package io.openems.edge.ess.gridvolt.gridvolt1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.edge.battery.api.Battery;
import io.openems.edge.batteryinverter.api.ManagedSymmetricBatteryInverter;
import io.openems.edge.batteryinverter.api.SymmetricBatteryInverter;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.startstop.StartStop;
import io.openems.edge.common.startstop.StartStoppable;
import io.openems.edge.ess.api.ManagedSymmetricEss;
import io.openems.edge.ess.api.SymmetricEss;
import io.openems.edge.ess.power.api.Power;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.ess.gridvolt.gridvolt1", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class GridVolt1Impl extends AbstractOpenemsModbusComponent implements GridVolt1, ManagedSymmetricEss, SymmetricEss, OpenemsComponent, EventHandler, ModbusComponent {

	private Config config = null;
	
	private final Logger log = LoggerFactory.getLogger(GridVolt1Impl.class);

	
	@Reference
	private ConfigurationAdmin cm;
	
	@Reference
	private ComponentManager componentManager;
	
	@Reference
	private Power power;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private ManagedSymmetricBatteryInverter batteryInverter;
	
	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	private Battery battery;
	
	public GridVolt1Impl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				StartStoppable.ChannelId.values(),
				SymmetricEss.ChannelId.values(),
				GridVolt1.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		
		// Configure Inverter 
		if (OpenemsComponent.updateReferenceFilter(this.cm, this.servicePid(), "batteryInverter", this.config.inverter_id())) {
			return;
		}
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			// TODO: fill channels
			break;
		}
	}

	@Override
	public String debugLog() {
		return "SoC:" + this.getSoc().asString() //
				+ "|L:" + this.getActivePower().asString() //
				+ "|Allowed:"
				+ this.channel(ManagedSymmetricEss.ChannelId.ALLOWED_CHARGE_POWER).value().asStringWithoutUnit() + ";"
				+ this.channel(ManagedSymmetricEss.ChannelId.ALLOWED_DISCHARGE_POWER).value().asString() //
				+ "|" + this.getGridModeChannel().value().asOptionString(); //
			//	+ "|Feed-In:"
			//	+ this.channel(EssFeneconCommercial40.ChannelId.SURPLUS_FEED_IN_STATE_MACHINE).value().asOptionString();

	}

	@Override
	public void setStartStop(StartStop value) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		
	}
	
	protected ManagedSymmetricBatteryInverter getBatteryInverter() {
		return this.batteryInverter;
	}

	protected Battery getBattery() {
		return this.battery;
	}

	@Override
	public String id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String alias() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ComponentContext getComponentContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Channel<?> _channel(String channelName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Channel<?>> channels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() throws OpenemsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Power getPower() {
		return this.power;
	}

	@Override
	public void applyPower(int activePower, int reactivePower) throws OpenemsNamedException {
		this.getBatteryInverter().run(this.getBattery(), activePower, reactivePower);
	}

	@Override
	public int getPowerPrecision() {
		return this.batteryInverter.getPowerPrecision();
	}

}
