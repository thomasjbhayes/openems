package io.openems.edge.controller.io.loadshedding;

import java.time.Duration;
import java.time.LocalDateTime;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.ChannelAddress;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.channel.WriteChannel;
import io.openems.edge.common.channel.value.Value;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.type.TypeUtils;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.meter.api.ElectricityMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Controller.io.openems.edge.controller.io.loadshedding", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
public class ControllerIoLoadSheddingImpl extends AbstractOpenemsComponent implements ControllerIoLoadShedding, Controller, OpenemsComponent {
	
	@Reference
	private ComponentManager componentManager;
	
	private Config config;
	
	private final Logger log = LoggerFactory.getLogger(ControllerIoLoadSheddingImpl.class);
	
	private int maxImportPower = 0; // Max import power before IO triggered

	
	// Channel addresses for each output
	private ChannelAddress outputChannel1Address = null;
	private ChannelAddress outputChannel2Address = null;
	private ChannelAddress outputChannel3Address = null;
	private ChannelAddress outputChannel4Address = null;
	private ChannelAddress outputChannel5Address = null;
	private ChannelAddress outputChannel6Address = null;
	private ChannelAddress outputChannel7Address = null;
	private ChannelAddress outputChannel8Address = null;
	
	private ChannelAddress generatorInputChannel = null;
	
	private Boolean generatorOn = false;
	
	private Boolean generatorProtectionLoadManagementEnabled = false;
		
	private LocalDateTime lastStateChangeTime = LocalDateTime.MIN;
	
	private SwitchingState switchingState = SwitchingState.UNDEFINED;

	public ControllerIoLoadSheddingImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				ControllerIoLoadShedding.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsNamedException {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		this.maxImportPower = config.importPowerThreshold() + config.importOverThresholdLimit();
		
		this.generatorProtectionLoadManagementEnabled = config.generatorProtectionLoadManagementEnabled();
		
		if (config.loadChannel1Enabled()) {
			this.outputChannel1Address = ChannelAddress.fromString(config.channelAddressLoad1());
		}
		if (config.loadChannel2Enabled()) {
			this.outputChannel2Address = ChannelAddress.fromString(config.channelAddressLoad2()); // TODO: Test how this handles missing addresses. E.g when channel not in use
		}
		if (config.loadChannel3Enabled()) {
			this.outputChannel3Address = ChannelAddress.fromString(config.channelAddressLoad3());
		}
		if (config.loadChannel4Enabled()) {
			this.outputChannel4Address = ChannelAddress.fromString(config.channelAddressLoad4());
		}
		if (config.loadChannel5Enabled()) {
			this.outputChannel5Address = ChannelAddress.fromString(config.channelAddressLoad5());
		}
		if (config.loadChannel6Enabled()) {
			this.outputChannel6Address = ChannelAddress.fromString(config.channelAddressLoad6()); // TODO: Test how this handles missing addresses. E.g when channel not in use
		}
		if (config.loadChannel7Enabled()) {
			this.outputChannel7Address = ChannelAddress.fromString(config.channelAddressLoad7());
		}
		if (config.loadChannel8Enabled()) {
			this.outputChannel8Address = ChannelAddress.fromString(config.channelAddressLoad8());
		}
		
		if (config.generatorProtectionLoadManagementEnabled()) {
			this.generatorInputChannel = ChannelAddress.fromString(config.channelAddressGeneratorOn());
		}
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void run() throws OpenemsNamedException {		
		log.info("Elapsed " + this.hasWaitTimeElapsed());
		
		SwitchingState nextSwitchingState = this.switchingState;
		if (this.hasWaitTimeElapsed()) { // Do nothing if the wait time hasn't elapsed
			
			nextSwitchingState = this.getNextSwitchingState();
		}
		this.changeSwitchingState(nextSwitchingState);
	}
	
	private SwitchingState getNextSwitchingState() throws OpenemsNamedException {
		
		SwitchingState nextSwitchingState = SwitchingState.UNDEFINED;
		int switchOffNextLoadThreshold = -1;
		int switchOnLoadThreshold = -1;
		
		ElectricityMeter gridMeter = this.componentManager.getComponent(this.config.gridMeterId());
		Value<Integer> gridActivePower = gridMeter.getActivePower();
		
		boolean stateSetByGeneratorTrigger = false; 
		
		log.info("Generator protection active: ".concat(this.generatorProtectionLoadManagementEnabled.toString()));
		
		if (this.generatorProtectionLoadManagementEnabled) {
			var generatorOnChannel = this.componentManager.getChannel(this.generatorInputChannel);
			Boolean generatorOnValueDefined = generatorOnChannel.value().isDefined();
			
			boolean isGeneratorOn = generatorOnValueDefined ? TypeUtils.getAsType(OpenemsType.BOOLEAN, generatorOnChannel.value().getOrError()) : false;
			
			if (generatorOnChannel.value().isDefined() && isGeneratorOn && this.generatorOn == false) { // If generator has 
				nextSwitchingState = SwitchingState.LOAD_8_SWITCHED_OFF;
				stateSetByGeneratorTrigger = true;
				this.generatorOn = true;
			}
		}
		
		if (!stateSetByGeneratorTrigger) {
			switch (this.switchingState) {
			case UNDEFINED: // At startup, either go for all loads on or off depending on config
				if (this.config.startupBehaviorPreference() == StartupBehavior.ALL_OFF) {
					nextSwitchingState = SwitchingState.LOAD_4_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.ALL_LOADS_ON;
				}
				break;
			case ALL_LOADS_ON:
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = -1; // This is not possible if all loads on
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_1_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.ALL_LOADS_ON;
				}
				break;
			case LOAD_1_SWITCHED_OFF:
				
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load1SwitchedPower();
				
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_2_SWITCHED_OFF;
				} else if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.ALL_LOADS_ON;
				} else {
					nextSwitchingState = SwitchingState.LOAD_1_SWITCHED_OFF;
				}
				
				break;
			case LOAD_2_SWITCHED_OFF:
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load2SwitchedPower();
				
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_3_SWITCHED_OFF;
				} else if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_1_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.LOAD_2_SWITCHED_OFF;
				}
				
				break;
			case LOAD_3_SWITCHED_OFF:
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load3SwitchedPower();
				
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_4_SWITCHED_OFF;
				} else if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_2_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.LOAD_3_SWITCHED_OFF;
				}
				
				break;
			case LOAD_4_SWITCHED_OFF:
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load4SwitchedPower();
				
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_5_SWITCHED_OFF;
				} else if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_3_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.LOAD_4_SWITCHED_OFF;
				}
				
				break;
			case LOAD_5_SWITCHED_OFF:
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load5SwitchedPower();
				
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_6_SWITCHED_OFF;
				} else if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_4_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.LOAD_5_SWITCHED_OFF;
				}
				break;
			case LOAD_6_SWITCHED_OFF:
				
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load6SwitchedPower();
				
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_7_SWITCHED_OFF;
				} else if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_5_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.LOAD_6_SWITCHED_OFF;
				}
				break;
			case LOAD_7_SWITCHED_OFF:
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load7SwitchedPower();
				
				if (gridActivePower.get() > switchOffNextLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_8_SWITCHED_OFF;
				} else if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_6_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.LOAD_7_SWITCHED_OFF;
				}
				break;
			case LOAD_8_SWITCHED_OFF:
				switchOffNextLoadThreshold = this.maxImportPower;
				switchOnLoadThreshold = this.maxImportPower - this.config.load8SwitchedPower();
				
				if (gridActivePower.get() < switchOnLoadThreshold) {
					nextSwitchingState = SwitchingState.LOAD_7_SWITCHED_OFF;
				} else {
					nextSwitchingState = SwitchingState.LOAD_8_SWITCHED_OFF;
				}
				break;
			default:
				break;
			}
		}
		
		log.info("Want to change state to... ".concat(nextSwitchingState.getName()));
		
		return getNextHighestAvailableSwitchingState(nextSwitchingState); // Go to the highest available state that is enabled
	}
	
	/**
	 * Returns the highest possible state that is available, depending on the states that are enabled in the config
	 * @param state
	 * @return
	 */
	private SwitchingState getNextHighestAvailableSwitchingState(SwitchingState state) {
		SwitchingState availableState = SwitchingState.UNDEFINED;
		
		switch (state) {
		case UNDEFINED:
			availableState = state; // We can always go to UNDEFINED (should never happen in practice)
			break;
		case ALL_LOADS_ON:
			availableState = state; // This is always possible even if certain outputs are disabled
		case LOAD_1_SWITCHED_OFF:
			if (this.config.loadChannel1Enabled()) {
				availableState = state;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		
		case LOAD_2_SWITCHED_OFF:
			if (this.config.loadChannel2Enabled()) {
				availableState = state;
			} else if (this.config.loadChannel1Enabled()) {
				availableState = SwitchingState.LOAD_1_SWITCHED_OFF;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		case LOAD_3_SWITCHED_OFF:
			if (this.config.loadChannel3Enabled()) {
				availableState = state;
			} else if (this.config.loadChannel2Enabled()) {
				availableState = SwitchingState.LOAD_2_SWITCHED_OFF;
			} else if (this.config.loadChannel1Enabled()) {
				availableState = SwitchingState.LOAD_1_SWITCHED_OFF;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		case LOAD_4_SWITCHED_OFF:
			if (this.config.loadChannel4Enabled()) {
				availableState = state;
			} else if (this.config.loadChannel3Enabled()) {
				availableState = SwitchingState.LOAD_3_SWITCHED_OFF;
			} else if (this.config.loadChannel2Enabled()) {
				availableState = SwitchingState.LOAD_2_SWITCHED_OFF;
			} else if (this.config.loadChannel1Enabled()) {
				availableState = SwitchingState.LOAD_1_SWITCHED_OFF;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		case LOAD_5_SWITCHED_OFF:
			if (this.config.loadChannel5Enabled()) {
				availableState = state;
			} else if (this.config.loadChannel4Enabled()) {
				availableState = SwitchingState.LOAD_4_SWITCHED_OFF;
			} else if (this.config.loadChannel3Enabled()) {
				availableState = SwitchingState.LOAD_3_SWITCHED_OFF;
			} else if (this.config.loadChannel2Enabled()) {
				availableState = SwitchingState.LOAD_2_SWITCHED_OFF;
			} else if (this.config.loadChannel1Enabled()) {
				availableState = SwitchingState.LOAD_1_SWITCHED_OFF;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		case LOAD_6_SWITCHED_OFF:
			if (this.config.loadChannel6Enabled()) {
				availableState = state;
			} else if (this.config.loadChannel5Enabled()) {
				availableState = SwitchingState.LOAD_5_SWITCHED_OFF;
			} else if (this.config.loadChannel4Enabled()) {
				availableState = SwitchingState.LOAD_4_SWITCHED_OFF;
			} else if (this.config.loadChannel3Enabled()) {
				availableState = SwitchingState.LOAD_3_SWITCHED_OFF;
			} else if (this.config.loadChannel2Enabled()) {
				availableState = SwitchingState.LOAD_2_SWITCHED_OFF;
			} else if (this.config.loadChannel1Enabled()) {
				availableState = SwitchingState.LOAD_1_SWITCHED_OFF;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		case LOAD_7_SWITCHED_OFF:
			if (this.config.loadChannel7Enabled()) {
				availableState = state;
			} else if (this.config.loadChannel6Enabled()) {
				availableState = SwitchingState.LOAD_6_SWITCHED_OFF;
			} else if (this.config.loadChannel5Enabled()) {
				availableState = SwitchingState.LOAD_5_SWITCHED_OFF;
			} else if (this.config.loadChannel4Enabled()) {
				availableState = SwitchingState.LOAD_4_SWITCHED_OFF;
			} else if (this.config.loadChannel3Enabled()) {
				availableState = SwitchingState.LOAD_3_SWITCHED_OFF;
			} else if (this.config.loadChannel2Enabled()) {
				availableState = SwitchingState.LOAD_2_SWITCHED_OFF;
			} else if (this.config.loadChannel1Enabled()) {
				availableState = SwitchingState.LOAD_1_SWITCHED_OFF;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		case LOAD_8_SWITCHED_OFF:
			if (this.config.loadChannel7Enabled()) {
				availableState = state;
			} else if (this.config.loadChannel7Enabled()) {
				availableState = SwitchingState.LOAD_7_SWITCHED_OFF;
			} else if (this.config.loadChannel6Enabled()) {
				availableState = SwitchingState.LOAD_6_SWITCHED_OFF;
			} else if (this.config.loadChannel5Enabled()) {
				availableState = SwitchingState.LOAD_5_SWITCHED_OFF;
			} else if (this.config.loadChannel4Enabled()) {
				availableState = SwitchingState.LOAD_4_SWITCHED_OFF;
			} else if (this.config.loadChannel3Enabled()) {
				availableState = SwitchingState.LOAD_3_SWITCHED_OFF;
			} else if (this.config.loadChannel2Enabled()) {
				availableState = SwitchingState.LOAD_2_SWITCHED_OFF;
			} else if (this.config.loadChannel1Enabled()) {
				availableState = SwitchingState.LOAD_1_SWITCHED_OFF;
			} else {
				availableState = SwitchingState.ALL_LOADS_ON;
			}
			break;
		default:
			availableState = state;
			break;
		}
		return availableState;
	}
	
	private void changeSwitchingState(SwitchingState nextState) throws OpenemsNamedException {
		switch(nextState) {
		case UNDEFINED:
			// If next state is undefined.. take no action?
			log.info("Next state: ".concat(nextState.toString()));
			break;
		case ALL_LOADS_ON:
			
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(false, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(false, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(false, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(false, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(false, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(false, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(false, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			break;
		case LOAD_1_SWITCHED_OFF:
			
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(false, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(false, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(false, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(false, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(false, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(false, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			
			break;
		case LOAD_2_SWITCHED_OFF:
			
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(true, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(false, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(false, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(false, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(false, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(false, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			
			break;
		case LOAD_3_SWITCHED_OFF:
			
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(true, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(true, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(false, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(false, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(false, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(false, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			
			break;
		case LOAD_4_SWITCHED_OFF:
			
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(true, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(true, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(true, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(false, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(false, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(false, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			
			break;
		case LOAD_5_SWITCHED_OFF:
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(true, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(true, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(true, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(true, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(false, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(false, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			break;
		case LOAD_6_SWITCHED_OFF:
			
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(true, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(true, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(true, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(true, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(true, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(false, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			break;
		case LOAD_7_SWITCHED_OFF:
			
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(true, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(true, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(true, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(true, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(true, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(true, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(false, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			break;
		case LOAD_8_SWITCHED_OFF:
			if (this.config.loadChannel1Enabled()) { // Don't set channel 1 if not enabled
				Boolean channel1Value = getValueForChannel(true, this.config.channel1LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel1Address, channel1Value);
			}
			
			if (this.config.loadChannel2Enabled()) { // Don't set channel 2 if not enabled
				Boolean channel2Value = getValueForChannel(true, this.config.channel2LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel2Address, channel2Value);
			}
			
			if (this.config.loadChannel3Enabled()) { // Don't set channel 3 if not enabled
				Boolean channel3Value = getValueForChannel(true, this.config.channel3LogicType()); // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel3Address, channel3Value);
			}
			
			if (this.config.loadChannel4Enabled()) { // Don't set channel 4 if not enabled
				Boolean channel4Value = getValueForChannel(true, this.config.channel4LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel4Address, channel4Value);
			}
			
			if (this.config.loadChannel5Enabled()) { // Don't set channel 5 if not enabled
				Boolean channel5Value = getValueForChannel(true, this.config.channel5LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel5Address, channel5Value);
			}
			
			if (this.config.loadChannel6Enabled()) { // Don't set channel 6 if not enabled
				Boolean channel6Value = getValueForChannel(true, this.config.channel6LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel6Address, channel6Value);
			}
			
			if (this.config.loadChannel7Enabled()) { // Don't set channel 7 if not enabled
				Boolean channel7Value = getValueForChannel(true, this.config.channel7LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel7Address, channel7Value);
			}
			
			if (this.config.loadChannel8Enabled()) { // Don't set channel 8 if not enabled
				Boolean channel8Value = getValueForChannel(true, this.config.channel8LogicType());; // If negative logic, then write 1, otherwise write 0				
				this.setChannel(this.outputChannel8Address, channel8Value);
			}
			break;
		}
		
		if (nextState != this.switchingState) {
			this.lastStateChangeTime = LocalDateTime.now(this.componentManager.getClock());
		}
		
		this.switchingState = nextState;
		
	}
	
	/**
	 * Sets the value of a given ChannelAddress to a boolean value
	 * @param channelAddress
	 * @param value
	 * @throws OpenemsNamedException
	 */
	private void setChannel(ChannelAddress channelAddress, Boolean value) throws OpenemsNamedException {
		WriteChannel<Boolean> channel = this.getOutputChannel(channelAddress);
		var currentChannelValue = channel.value();
		log.info("Current channel value is...".concat((currentChannelValue.get() != null ? currentChannelValue.get().toString() : "null")));
		log.info("Changing to...".concat(value.toString()));
		if (!currentChannelValue.isDefined() || currentChannelValue.get() != value) {
			channel.setNextWriteValue(value);
		}
	}
	
	/**
	 * Boolean value to set write-channel to depends on logic type.
	 * @param channelTriggered
	 * @param channelLogicType
	 * @return
	 */
	private Boolean getValueForChannel(Boolean channelTriggered, LogicType channelLogicType) {
		return channelTriggered ^ (channelLogicType == LogicType.NEGATIVE);
	}
	
	/**
	 * Checks if the config.waitTime seconds have elapsed since the last state change
	 * @return
	 */
	private boolean hasWaitTimeElapsed() {
		Duration hysteresisTime = Duration.ofSeconds(this.config.waitTime());
		return this.lastStateChangeTime.plus(hysteresisTime).isBefore(LocalDateTime.now(this.componentManager.getClock()));
	}
	
	private WriteChannel<Boolean> getOutputChannel(ChannelAddress channelAddress) throws OpenemsNamedException {
		WriteChannel<Boolean> channel = null;
		OpenemsNamedException exceptionHappened = null;
		try {
			channel = this.componentManager.getChannel(channelAddress);
		} catch (OpenemsNamedException e) {
			e.printStackTrace();
			exceptionHappened = e;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			exceptionHappened = new OpenemsException(e.getMessage());
		}
		if (exceptionHappened != null) {
			throw exceptionHappened;
		}
		return channel;
	}
	
}
