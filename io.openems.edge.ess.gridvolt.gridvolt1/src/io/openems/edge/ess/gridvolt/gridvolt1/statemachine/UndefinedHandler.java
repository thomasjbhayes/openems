package io.openems.edge.ess.gridvolt.gridvolt1.statemachine;

import io.openems.edge.common.statemachine.StateHandler;
import io.openems.edge.ess.gridvolt.gridvolt1.GridVolt1;
import io.openems.edge.ess.gridvolt.gridvolt1.statemachine.StateMachine.State;

public class UndefinedHandler extends StateHandler<State, Context> {
	
	@Override
	public State runAndGetNextState(Context context) {
		GridVolt1 ess = context.getParent();
		
		switch (ess.getStartStopTarget()) {
		case UNDEFINED:
			return State.UNDEFINED;	
		case START:
			if (ess.hasFaults()) { // Are battery/inverter faults included TODO
				return State.ERROR;
			} else {
				return State.START_BATTERY;
			}

		case STOP:
			return State.STOP_BATTERY_INVERTER;
		
		
		}
		assert false;
		return State.UNDEFINED;
	}
}
