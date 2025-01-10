package io.openems.edge.controller.io.loadshedding;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Controller IO Loadshedding", //
		description = "Controls up to 4 digital outputs used to shed loads based on grid import restrictions")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlLoadShedding0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	// Meter
	@AttributeDefinition(name = "Grid Meter ID", description = "ID of Grid Meter")
	String gridMeterId() default "meter0";
	
	// Import power threshold
	@AttributeDefinition(name = "Import power threshold (W)", description = "Threshold of power import from grid before load shedding (e.g MIC)")
	int importPowerThreshold() default 10000;
	
	// Additional power before shedding load
	@AttributeDefinition(name = "Import power over threshold limit (W)", description = "Limit of power import over threshold before load shedding occurs")
	int importOverThresholdLimit() default 1000;
	
	// Wait time between triggering I/Os
	@AttributeDefinition(name = "Wait time (s)", description = "Wait time (in seconds) between taking actions / triggering I/Os")
	int waitTime() default 300;
	
	@AttributeDefinition(name = "Start-up Behavior", description = "Should the system start with all loads on, or start with all-off and turn on sequentially")
	StartupBehavior startupBehaviorPreference() default StartupBehavior.ALL_ON;
	
	
	// Load channel 1 config
	@AttributeDefinition(name = "Load channel 1 enabled?", description = "Is load channel 1 active for this controller?")
	boolean loadChannel1Enabled() default true;
	
	@AttributeDefinition(name = "Load 1 name", description = "Load 1 name.")
	String load1Name() default "Load 1";
	
	@AttributeDefinition(name = "Output Channel 1", description = "Channel address of the Digital Output for load 1.")
	String channelAddressLoad1() default "io0/Relay1";
	
	@AttributeDefinition(name = "Channel 1 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel1LogicType() default LogicType.POSITIVE;
	
	@AttributeDefinition(name = "Load 1 - Switched Power (W)", description = "Power of the load switched by output 1 (used to avoid immediate switching load back on).")
	int load1SwitchedPower() default 0;
	
	// Load channel 2 config
	@AttributeDefinition(name = "Load channel 2 enabled?", description = "Is load channel 2 active for this controller?")
	boolean loadChannel2Enabled() default false;
	
	@AttributeDefinition(name = "Load 2 name", description = "Load 2 name.")
	String load2Name() default "Load 2";

	@AttributeDefinition(name = "Output Channel 2", description = "Channel address of the Digital Output for load 2.")
	String channelAddressLoad2() default "io0/Relay2";
	
	@AttributeDefinition(name = "Channel 2 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel2LogicType() default LogicType.POSITIVE;
	
	@AttributeDefinition(name = "Load 2 - Switched Power (W)", description = "Power of the load switched by output 2 (used to avoid immediate switching load back on).")
	int load2SwitchedPower() default 0;
	
	
	// Load channel 3 config
	@AttributeDefinition(name = "Load channel 3 enabled?", description = "Is load channel 3 active for this controller?")
	boolean loadChannel3Enabled() default false;
	
	@AttributeDefinition(name = "Load 3 name", description = "Load 3 name.")
	String load3Name() default "Load 3";
	
	@AttributeDefinition(name = "Output Channel 3", description = "Channel address of the Digital Output for load 3.")
	String channelAddressLoad3() default "io0/Relay3";
	
	@AttributeDefinition(name = "Channel 3 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel3LogicType() default LogicType.POSITIVE;
	
	@AttributeDefinition(name = "Load 3 - Switched Power (W)", description = "Power of the load switched by output 3 (used to avoid immediate switching load back on).")
	int load3SwitchedPower() default 0;

	
	// Load channel 4 config
	@AttributeDefinition(name = "Load channel 4 enabled?", description = "Is load channel 4 active for this controller?")
	boolean loadChannel4Enabled() default false;
	
	@AttributeDefinition(name = "Load 4 name", description = "Load 4 name.")
	String load4Name() default "Load 4";
	
	@AttributeDefinition(name = "Output Channel 4", description = "Channel address of the Digital Output for load 4.")
	String channelAddressLoad4() default "io0/Relay4";
	
	@AttributeDefinition(name = "Channel 4 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel4LogicType() default LogicType.POSITIVE;
	
	@AttributeDefinition(name = "Load 4 - Switched Power (W)", description = "Power of the load switched by output 4 (used to avoid immediate switching load back on).")
	int load4SwitchedPower() default 0;
	
	// Load channel 5 config
	@AttributeDefinition(name = "Load channel 5 enabled?", description = "Is load channel 5 active for this controller?")
	boolean loadChannel5Enabled() default false;
		
	@AttributeDefinition(name = "Load 5 name", description = "Load 5 name.")
	String load5Name() default "Load 5";
		
	@AttributeDefinition(name = "Output Channel 5", description = "Channel address of the Digital Output for load 5.")
	String channelAddressLoad5() default "io0/Relay5";
		
	@AttributeDefinition(name = "Channel 5 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel5LogicType() default LogicType.POSITIVE;
		
	@AttributeDefinition(name = "Load 5 - Switched Power (W)", description = "Power of the load switched by output 5 (used to avoid immediate switching load back on).")
	int load5SwitchedPower() default 0;
	
	// Load channel 6 config
	@AttributeDefinition(name = "Load channel 6 enabled?", description = "Is load channel 6 active for this controller?")
	boolean loadChannel6Enabled() default false;
		
	@AttributeDefinition(name = "Load 6 name", description = "Load 6 name.")
	String load6Name() default "Load 6";
		
	@AttributeDefinition(name = "Output Channel 6", description = "Channel address of the Digital Output for load 6.")
	String channelAddressLoad6() default "io0/Relay6";
		
	@AttributeDefinition(name = "Channel 6 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel6LogicType() default LogicType.POSITIVE;
		
	@AttributeDefinition(name = "Load 6 - Switched Power (W)", description = "Power of the load switched by output 6 (used to avoid immediate switching load back on).")
	int load6SwitchedPower() default 0;
	
	// Load channel 7 config
	@AttributeDefinition(name = "Load channel 7 enabled?", description = "Is load channel 7 active for this controller?")
	boolean loadChannel7Enabled() default false;
			
	@AttributeDefinition(name = "Load 7 name", description = "Load 7 name.")
	String load7Name() default "Load 7";
			
	@AttributeDefinition(name = "Output Channel 7", description = "Channel address of the Digital Output for load 7.")
	String channelAddressLoad7() default "io0/Relay7";
			
	@AttributeDefinition(name = "Channel 7 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel7LogicType() default LogicType.POSITIVE;
			
	@AttributeDefinition(name = "Load 7 - Switched Power (W)", description = "Power of the load switched by output 7 (used to avoid immediate switching load back on).")
	int load7SwitchedPower() default 0;
	
	// Load channel 7 config
	@AttributeDefinition(name = "Load channel 8 enabled?", description = "Is load channel 8 active for this controller?")
	boolean loadChannel8Enabled() default false;
				
	@AttributeDefinition(name = "Load 8 name", description = "Load 8 name.")
	String load8Name() default "Load 8";
				
	@AttributeDefinition(name = "Output Channel 8", description = "Channel address of the Digital Output for load 8.")
	String channelAddressLoad8() default "io0/Relay8";
				
	@AttributeDefinition(name = "Channel 8 - logic type", description = "Positive logic (output ON = drop load), or negative logic (output OFF = drop load)")
	LogicType channel8LogicType() default LogicType.POSITIVE;
				
	@AttributeDefinition(name = "Load 8 - Switched Power (W)", description = "Power of the load switched by output 8 (used to avoid immediate switching load back on).")
	int load8SwitchedPower() default 0;
	
	
	// Generator load management
	@AttributeDefinition(name = "Generator protection load management enabled?", description = "Drop managed loads during generator startup")
	boolean generatorProtectionLoadManagementEnabled() default false;
	
	@AttributeDefinition(name = "Generator input channel address", description = "Channel address of the Digital Input that signals generator-on.")
	String channelAddressGeneratorOn() default "io0/Input1";
	
	

	String webconsole_configurationFactory_nameHint() default "Controller IO Load Shedding [{id}]";

}