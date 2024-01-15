package io.openems.edge.ess.gridvolt.gridvolt1.statemachine;

import java.time.Duration;
import java.time.Instant;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.statemachine.StateHandler;
import io.openems.edge.ess.gridvolt.gridvolt1.GridVolt1;
import io.openems.edge.ess.gridvolt.gridvolt1.statemachine.StateMachine.State;

public class StartBatteryInverterHandler extends StateHandler<State, Context> {
	
	private Instant lastAttempt = Instant.MIN;
	private int attemptCounter = 0;
	
	@Override
	protected void onEntry(Context context) throws OpenemsNamedException {
		this.lastAttempt = Instant.MIN;
		this.attemptCounter = 0;
		GridVolt1 ess = context.getParent();
		ess._setMaxBatteryStartAttemptsFault(false);
	}
	
	@Override
	public State runAndGetNextState(Context context) throws OpenemsNamedException {
		GridVolt1 ess = context.getParent();
		
		if (context.getBattery().isStarted()) {
			return State.STARTED;
		}
		
		Boolean isMaxStartTimePassed = Duration.between(this.lastAttempt, Instant.now())
				.getSeconds() > GridVolt1.RETRY_COMMAND_SECONDS;
				
		if (!isMaxStartTimePassed) {
			// Waiting for start
			return State.START_BATTERY_INVERTER;
		}
		if (this.attemptCounter > GridVolt1.RETRY_COMMAND_MAX_ATTEMPTS) {
			// Too many tries - GOTO Undefined
			ess._setMaxBatteryStartAttemptsFault(true);
			return State.UNDEFINED;
		} else {
			context.batteryInverter.start();
			
			this.lastAttempt = Instant.now();
			this.attemptCounter++;
			return State.START_BATTERY_INVERTER;
		}
	}
}

