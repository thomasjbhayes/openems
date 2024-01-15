package io.openems.edge.ess.gridvolt.gridvolt1.statemachine;

import io.openems.edge.common.startstop.StartStop;
import io.openems.edge.common.statemachine.StateHandler;
import io.openems.edge.ess.gridvolt.gridvolt1.GridVolt1;
import io.openems.edge.ess.gridvolt.gridvolt1.statemachine.StateMachine.State;

public class StartedHandler extends StateHandler<State, Context> {
	
	@Override
	public State runAndGetNextState(Context context) {
		GridVolt1 ess = context.getParent();
		
		if (ess.hasFaults()) {
			return State.UNDEFINED;
		}
		if (!context.battery.isStarted()) {
			return State.UNDEFINED;
		}
		if (!context.batteryInverter.isStarted()) {
			return State.UNDEFINED;
		}
		
		ess._setStartStop(StartStop.START);
		
		return State.STARTED;
	}
}
