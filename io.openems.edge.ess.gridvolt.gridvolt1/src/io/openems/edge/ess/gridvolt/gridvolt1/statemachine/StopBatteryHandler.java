package io.openems.edge.ess.gridvolt.gridvolt1.statemachine;

import java.time.Duration;
import java.time.Instant;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.edge.common.statemachine.StateHandler;
import io.openems.edge.ess.gridvolt.gridvolt1.GridVolt1;
import io.openems.edge.ess.gridvolt.gridvolt1.statemachine.StateMachine.State;

public class StopBatteryHandler extends StateHandler<State, Context> {
	
	private Instant lastAttempt = Instant.MIN;
	private int attemptCounter = 0;
	
	@Override
	protected void onEntry(Context context) throws OpenemsNamedException {
		this.lastAttempt = Instant.MIN;
		this.attemptCounter = 0;
		GridVolt1 ess = context.getParent();
		ess._setMaxBatteryStopAttemptsFault(false);
	}
	
	@Override
	public State runAndGetNextState(Context context) throws OpenemsNamedException {
		
		GridVolt1 ess = context.getParent();
		
		if (context.battery.isStopped()) {
			return State.STOPPED;
		}
		
		Boolean isMaxStopTimePassed = Duration.between(this.lastAttempt, Instant.now())
				.getSeconds() > GridVolt1.RETRY_COMMAND_SECONDS;
		
		if (!isMaxStopTimePassed) {
			return State.STOP_BATTERY;
		}
		if (this.attemptCounter > GridVolt1.RETRY_COMMAND_MAX_ATTEMPTS) {
			ess._setMaxBatteryStopAttemptsFault(true);
			return State.UNDEFINED;
		} else {
			context.battery.stop();
			this.lastAttempt = Instant.now();
			this.attemptCounter++;
			return State.STOP_BATTERY;
		}
	}
}
