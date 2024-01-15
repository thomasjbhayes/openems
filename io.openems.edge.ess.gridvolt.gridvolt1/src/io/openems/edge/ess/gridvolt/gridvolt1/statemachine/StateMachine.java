package io.openems.edge.ess.gridvolt.gridvolt1.statemachine;

import com.google.common.base.CaseFormat;

import io.openems.common.types.OptionsEnum;
import io.openems.edge.common.statemachine.AbstractStateMachine;
import io.openems.edge.common.statemachine.StateHandler;

public class StateMachine extends AbstractStateMachine<StateMachine.State, Context> {
	
	public enum State implements io.openems.edge.common.statemachine.State<State>, OptionsEnum {
		UNDEFINED(-1),
		
		START_BATTERY(10), //
		START_BATTERY_INVERTER(11), //
		START_CCU(12), //
		STARTED(13), //

		STOP_BATTERY_INVERTER(20), //
		STOP_BATTERY(21), //
		STOP_CCU(22), //
		STOPPED(23), //

		ERROR(30), //
		;
		
		private final int value;
		
		private State(int value) {
			this.value = value;
		}

		@Override
		public int getValue() {
			return this.value;
		}
		
		@Override
		public String getName() {
			return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.name());
		}
		
		@Override
		public OptionsEnum getUndefined() {
			return UNDEFINED;
		}
		
		@Override
		public State[] getStates() {
			return State.values();
		}

	}
	
	public StateMachine(State initialState) {
		super(initialState);
	}

	@Override
	public StateHandler<State, Context> getStateHandler(State state) {
		switch (state) {
		case UNDEFINED:
			return new UndefinedHandler();
		case START_BATTERY:
			return new StartBatteryHandler();
		case START_BATTERY_INVERTER:
			return new StartBatteryInverterHandler();
		case START_CCU:
			return new StartCCUHandler();
		case STARTED:
			return new StartedHandler();
		case STOP_BATTERY_INVERTER:
			return new StopBatteryInverterHandler();
		case STOP_BATTERY:
			return new StopBatteryHandler();
		case STOP_CCU:
			return new StopCCUHandler();
		case STOPPED:
			return new StoppedHandler();
		case ERROR:
			return new ErrorHandler();
		}
		
		throw new IllegalArgumentException("Unknown State [" + state + "]");
	}
	
	}
