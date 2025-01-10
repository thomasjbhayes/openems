package io.openems.edge.controller.io.loadshedding;

import org.junit.Test;

import java.time.Instant;
import java.time.ZoneOffset;

import static java.time.temporal.ChronoUnit.SECONDS;

import static io.openems.edge.io.test.DummyInputOutput.ChannelId.INPUT_OUTPUT1;
import static io.openems.edge.io.test.DummyInputOutput.ChannelId.INPUT_OUTPUT2;
import static io.openems.edge.io.test.DummyInputOutput.ChannelId.INPUT_OUTPUT3;
import static io.openems.edge.io.test.DummyInputOutput.ChannelId.INPUT_OUTPUT4;
import static io.openems.edge.io.test.DummyInputOutput.ChannelId.INPUT_OUTPUT5;
import static io.openems.edge.io.test.DummyInputOutput.ChannelId.INPUT_OUTPUT9;
import static io.openems.edge.meter.api.ElectricityMeter.ChannelId.ACTIVE_POWER;

import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.common.test.TimeLeapClock;
import io.openems.edge.common.test.DummyComponentManager;
import io.openems.edge.controller.test.ControllerTest;
import io.openems.edge.io.test.DummyInputOutput;
import io.openems.edge.meter.test.DummyElectricityMeter;

public class ControllerIoLoadSheddingTest {

	@Test
	public void loadManagementTest() throws Exception {
		
		final var clock = new TimeLeapClock(Instant.parse("2020-01-01T01:00:00.00Z"), ZoneOffset.UTC);
		final var cm = new DummyComponentManager(clock);
		
		new ControllerTest(new ControllerIoLoadSheddingImpl()) //
				.addReference("componentManager", cm) //
				.addComponent(new DummyElectricityMeter("meter0"))
				.addComponent(new DummyInputOutput("io0"))
				.activate(MyConfig.create() //
						.setId("ctrl0") //
						.setGridMeterId("meter0")
						.setWaitTime(0)
						.setImportPowerThreshold(10000)
						.setImportOverThresholdLimit(1000)
						.setStartupBehaviorPreference(StartupBehavior.ALL_ON)
						.setLoadChannel1Enabled(true)
						.setChannelAddressLoad1("io0/InputOutput1")
						.setLoad1SwitchedPower(2000)
						.setChannel1LogicType(LogicType.POSITIVE)
						.setLoadChannel2Enabled(true) // Only one  output
						.setChannelAddressLoad2("io0/InputOutput2")
						.build()) //
				.next(new TestCase("Start off")
						.input("meter0", ACTIVE_POWER, 6000)
						.output("io0", INPUT_OUTPUT1, false)
						) //
				.next(new TestCase("Check it changes when power 15000W")
						.timeleap(clock, 2, SECONDS)
						.input("meter0", ACTIVE_POWER, 15000)
						.output("io0", INPUT_OUTPUT1, true)
						)
				.next(new TestCase()
						.timeleap(clock, 2, SECONDS)
						.input("meter0", ACTIVE_POWER, 9000)
						.output("io0", INPUT_OUTPUT1, true)
						) //
				.next(new TestCase()
						.timeleap(clock, 2, SECONDS)
						.input("meter0", ACTIVE_POWER, 7000)
						.output("io0", INPUT_OUTPUT1, false)
						)
				.next(new TestCase("Check it turns off Load 1")
						.timeleap(clock, 2, SECONDS)
						.input("meter0", ACTIVE_POWER, 12000)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, false)
						)
				.next(new TestCase("Check it turns off Load 2")
						.timeleap(clock, 2, SECONDS)
						.input("meter0", ACTIVE_POWER, 12000)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, true)
						)
				.next(new TestCase("Check it turns on Load 2 but keeps 1 off")
						.timeleap(clock, 2, SECONDS)
						.input("meter0", ACTIVE_POWER, 7000)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, false)
						)
				.deactivate();
	}
	
	@Test
	public void generatorTest2Stage() throws Exception {
		
		final var clock = new TimeLeapClock(Instant.parse("2020-01-01T01:00:00.00Z"), ZoneOffset.UTC);
		final var cm = new DummyComponentManager(clock);
		
		new ControllerTest(new ControllerIoLoadSheddingImpl()) //
				.addReference("componentManager", cm) //
				.addComponent(new DummyElectricityMeter("meter0"))
				.addComponent(new DummyInputOutput("io0"))
				.activate(MyConfig.create() //
						.setId("ctrl0") //
						.setGridMeterId("meter0")
						.setWaitTime(0)
						.setImportPowerThreshold(10000)
						.setImportOverThresholdLimit(1000)
						.setStartupBehaviorPreference(StartupBehavior.ALL_ON)
						.setLoadChannel1Enabled(true)
						.setChannelAddressLoad1("io0/InputOutput1")
						.setLoad1SwitchedPower(2000)
						.setChannel1LogicType(LogicType.POSITIVE)
						.setLoadChannel2Enabled(true) // Only one  output
						.setChannelAddressLoad2("io0/InputOutput2")
						.setGeneratorProtectionLoadManagementEnabled(true)
						.setChannelAddressGeneratorOn("io0/InputOutput9")
						.build()) //
				.next(new TestCase("Start off")
						//.input("io0", INPUT_OUTPUT9, false)
						.input("meter0", ACTIVE_POWER, 6000)
						.output("io0", INPUT_OUTPUT1, false)
						) //
				.next(new TestCase("Check will switch off load 1 first")
						.timeleap(clock, 2, SECONDS)
						.input("meter0", ACTIVE_POWER, 15000)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, false)
						)
				.next(new TestCase("Check generator activation will turn off all")
						.timeleap(clock, 2, SECONDS)
						.input("io0", INPUT_OUTPUT9, true)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, true)
						)
				.next(new TestCase("Check load 2 will switch on after time delay")
						.input("meter0", ACTIVE_POWER, 6000)
						.timeleap(clock, 2, SECONDS)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, false)
						)
				
				.deactivate();
	} 
	
	
	@Test
	public void generatorTest6Stage() throws Exception {
		
		final var clock = new TimeLeapClock(Instant.parse("2020-01-01T01:00:00.00Z"), ZoneOffset.UTC);
		final var cm = new DummyComponentManager(clock);
		
		new ControllerTest(new ControllerIoLoadSheddingImpl()) //
				.addReference("componentManager", cm) //
				.addComponent(new DummyElectricityMeter("meter0"))
				.addComponent(new DummyInputOutput("io0"))
				.activate(MyConfig.create() //
						.setId("ctrl0") //
						.setGridMeterId("meter0")
						.setWaitTime(0)
						.setImportPowerThreshold(10000)
						.setImportOverThresholdLimit(1000)
						.setStartupBehaviorPreference(StartupBehavior.ALL_ON)
						.setLoadChannel1Enabled(true) // Load 1
						.setChannelAddressLoad1("io0/InputOutput1")
						.setLoad1SwitchedPower(2000)
						.setChannel1LogicType(LogicType.POSITIVE)
						.setLoadChannel2Enabled(true) 
						.setChannelAddressLoad2("io0/InputOutput2")
						.setLoad2SwitchedPower(2000)
						.setChannel2LogicType(LogicType.POSITIVE)
						.setLoadChannel3Enabled(true) 
						.setChannelAddressLoad3("io0/InputOutput3")
						.setLoad3SwitchedPower(2000)
						.setChannel3LogicType(LogicType.POSITIVE)
						.setLoadChannel4Enabled(true) 
						.setChannelAddressLoad4("io0/InputOutput4")
						.setLoad4SwitchedPower(2000)
						.setChannel4LogicType(LogicType.POSITIVE)
						.setLoadChannel5Enabled(true) 
						.setChannelAddressLoad5("io0/InputOutput5")
						.setLoad5SwitchedPower(2000)
						.setChannel5LogicType(LogicType.POSITIVE)
						.setGeneratorProtectionLoadManagementEnabled(true)
						.setChannelAddressGeneratorOn("io0/InputOutput9")
						.build()) //
				.next(new TestCase("Start off")
						//.input("io0", INPUT_OUTPUT9, false)
						.input("meter0", ACTIVE_POWER, 6000)
						.output("io0", INPUT_OUTPUT1, false)
						.output("io0", INPUT_OUTPUT2, false)
						.output("io0", INPUT_OUTPUT3, false)
						.output("io0", INPUT_OUTPUT4, false)
						.output("io0", INPUT_OUTPUT5, false)
						) //
				.next(new TestCase("Check generator activation will turn off all")
						.timeleap(clock, 2, SECONDS)
						.input("io0", INPUT_OUTPUT9, true)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, true)
						.output("io0", INPUT_OUTPUT3, true)
						.output("io0", INPUT_OUTPUT4, true)
						.output("io0", INPUT_OUTPUT5, true)
						)
				.next(new TestCase("Check load 2 will switch on after time delay")
						.input("meter0", ACTIVE_POWER, 6000)
						.timeleap(clock, 2, SECONDS)
						.output("io0", INPUT_OUTPUT1, true)
						.output("io0", INPUT_OUTPUT2, true)
						.output("io0", INPUT_OUTPUT3, true)
						.output("io0", INPUT_OUTPUT4, true)
						.output("io0", INPUT_OUTPUT5, false)
						)
				
				.deactivate();
	} 

}
