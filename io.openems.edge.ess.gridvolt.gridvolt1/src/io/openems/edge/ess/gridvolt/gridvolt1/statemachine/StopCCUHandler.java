package io.openems.edge.ess.gridvolt.gridvolt1.statemachine;

import io.openems.edge.common.statemachine.StateHandler;
import io.openems.edge.ess.gridvolt.gridvolt1.statemachine.StateMachine.State;

public class StopCCUHandler extends StateHandler<State, Context> {
	@Override
	public State runAndGetNextState(Context context) {
		return State.UNDEFINED;
	}
}
