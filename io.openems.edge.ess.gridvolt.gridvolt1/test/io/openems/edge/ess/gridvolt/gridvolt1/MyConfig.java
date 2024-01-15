package io.openems.edge.ess.gridvolt.gridvolt1;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.edge.common.startstop.StartStopConfig;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
//		private String setting0;

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

//		public Builder setSetting0(String setting0) {
//			this.setting0 = setting0;
//			return this;
//		}

		public MyConfig build() {
			return new MyConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 * 
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String inverter_id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String battery_id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String modbus_id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int modbusUnitId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String Modbus_target() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StartStopConfig startStop() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public String setting0() {
//		return this.builder.setting0;
//	}

}