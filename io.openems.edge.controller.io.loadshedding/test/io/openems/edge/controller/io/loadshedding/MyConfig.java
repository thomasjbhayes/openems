package io.openems.edge.controller.io.loadshedding;

import io.openems.common.test.AbstractComponentConfig;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
		private String gridMeterId;
		private int importPowerThreshold = 10000;
		private int importOverThresholdLimit = 1000;
		private int waitTime = 60;
		private StartupBehavior startupBehaviorPreference = StartupBehavior.ALL_ON;
		
		private boolean loadChannel1Enabled = true;
		private String load1Name = "";
		private String channelAddressLoad1 = "";
		private LogicType channel1LogicType = LogicType.POSITIVE;
		private int load1SwitchedPower = 0;
		
		private boolean loadChannel2Enabled = false;
		private String load2Name = "";
		private String channelAddressLoad2 = "";
		private LogicType channel2LogicType = LogicType.POSITIVE;
		private int load2SwitchedPower = 0;
		
		private boolean loadChannel3Enabled = false;
		private String load3Name = "";
		private String channelAddressLoad3 = "";
		private LogicType channel3LogicType = LogicType.POSITIVE;
		private int load3SwitchedPower = 0;
		
		private boolean loadChannel4Enabled = false;
		private String load4Name = "";
		private String channelAddressLoad4 = "";
		private LogicType channel4LogicType = LogicType.POSITIVE;
		private int load4SwitchedPower = 0;
		
		private boolean loadChannel5Enabled = false;
		private String load5Name = "";
		private String channelAddressLoad5 = "";
		private LogicType channel5LogicType = LogicType.POSITIVE;
		private int load5SwitchedPower = 0;
		
		private boolean loadChannel6Enabled = false;
		private String load6Name = "";
		private String channelAddressLoad6 = "";
		private LogicType channel6LogicType = LogicType.POSITIVE;
		private int load6SwitchedPower = 0;
		
		private boolean loadChannel7Enabled = false;
		private String load7Name = "";
		private String channelAddressLoad7 = "";
		private LogicType channel7LogicType = LogicType.POSITIVE;
		private int load7SwitchedPower = 0;
		
		private boolean loadChannel8Enabled = false;
		private String load8Name = "";
		private String channelAddressLoad8 = "";
		private LogicType channel8LogicType = LogicType.POSITIVE;
		private int load8SwitchedPower = 0;
		
		private boolean generatorProtectionLoadManagementEnabled = false;
		private String channelAddressGeneratorOn = "";

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}
		
		public Builder setGridMeterId(String gridMeterId) {
			this.gridMeterId = gridMeterId;
			return this;
		}
		
		public Builder setImportPowerThreshold(int importPowerThreshold) {
			this.importPowerThreshold = importPowerThreshold;
			return this;
		}
		
		public Builder setImportOverThresholdLimit(int importOverThresholdLimit) {
			this.importOverThresholdLimit = importOverThresholdLimit;
			return this;
		}
		
		public Builder setWaitTime(int waitTime) {
			this.waitTime = waitTime;
			return this;
		}
		
		public Builder setStartupBehaviorPreference(StartupBehavior startupBehaviorPreference) {
			this.startupBehaviorPreference = startupBehaviorPreference;
			return this;
		}
		
		// Load 1 Methods
		public Builder setLoadChannel1Enabled(Boolean loadChannel1Enabled) {
			this.loadChannel1Enabled = loadChannel1Enabled;
			return this;
		}
		
		public Builder setLoad1Name(String load1Name) {
			this.load1Name = load1Name;
			return this;
		}
		
		public Builder setChannelAddressLoad1(String channelAddressLoad1) {
			this.channelAddressLoad1 = channelAddressLoad1;
			return this;
		}
		
		public Builder setChannel1LogicType(LogicType channel1LogicType) {
			this.channel1LogicType = channel1LogicType;
			return this;
		}
		
		public Builder setLoad1SwitchedPower(int load1SwitchedPower) {
			this.load1SwitchedPower = load1SwitchedPower;
			return this;
		}
		
		// Load 2 Methods
		public Builder setLoadChannel2Enabled(Boolean loadChannel2Enabled) {
			this.loadChannel2Enabled = loadChannel2Enabled;
			return this;
		}
		
		public Builder setLoad2Name(String load2Name) {
			this.load2Name = load2Name;
			return this;
		}
		
		public Builder setChannelAddressLoad2(String channelAddressLoad2) {
			this.channelAddressLoad2 = channelAddressLoad2;
			return this;
		}
		
		public Builder setChannel2LogicType(LogicType channel2LogicType) {
			this.channel2LogicType = channel2LogicType;
			return this;
		}
		
		public Builder setLoad2SwitchedPower(int load2SwitchedPower) {
			this.load2SwitchedPower = load2SwitchedPower;
			return this;
		}
		
		// Load 3 Methods
		public Builder setLoadChannel3Enabled(Boolean loadChannel3Enabled) {
	        this.loadChannel3Enabled = loadChannel3Enabled;
	        return this;
	    }
		
		public Builder setLoad3Name(String load3Name) {
			this.load3Name = load3Name;
			return this;
		}

	    public Builder setChannelAddressLoad3(String channelAddressLoad3) {
	        this.channelAddressLoad3 = channelAddressLoad3;
	        return this;
	    }

	    public Builder setChannel3LogicType(LogicType channel3LogicType) {
	        this.channel3LogicType = channel3LogicType;
	        return this;
	    }

	    public Builder setLoad3SwitchedPower(int load3SwitchedPower) {
	        this.load3SwitchedPower = load3SwitchedPower;
	        return this;
	    }

	    // Load 4 Methods
	    public Builder setLoadChannel4Enabled(Boolean loadChannel4Enabled) {
	        this.loadChannel4Enabled = loadChannel4Enabled;
	        return this;
	    }
	    
	    public Builder setLoad4Name(String load4Name) {
			this.load4Name = load4Name;
			return this;
		}

	    public Builder setChannelAddressLoad4(String channelAddressLoad4) {
	        this.channelAddressLoad4 = channelAddressLoad4;
	        return this;
	    }

	    public Builder setChannel4LogicType(LogicType channel4LogicType) {
	        this.channel4LogicType = channel4LogicType;
	        return this;
	    }

	    public Builder setLoad4SwitchedPower(int load4SwitchedPower) {
	        this.load4SwitchedPower = load4SwitchedPower;
	        return this;
	    }
	    
	    // Load 5 Methods
	    public Builder setLoadChannel5Enabled(Boolean loadChannel5Enabled) {
	        this.loadChannel5Enabled = loadChannel5Enabled;
	        return this;
	    }
	    
	    public Builder setLoad5Name(String load5Name) {
			this.load5Name = load5Name;
			return this;
		}

	    public Builder setChannelAddressLoad5(String channelAddressLoad5) {
	        this.channelAddressLoad5 = channelAddressLoad5;
	        return this;
	    }

	    public Builder setChannel5LogicType(LogicType channel5LogicType) {
	        this.channel5LogicType = channel5LogicType;
	        return this;
	    }

	    public Builder setLoad5SwitchedPower(int load5SwitchedPower) {
	        this.load5SwitchedPower = load5SwitchedPower;
	        return this;
	    }
	    
	 // Load 6 Methods
	    public Builder setLoadChannel6Enabled(Boolean loadChannel6Enabled) {
	        this.loadChannel6Enabled = loadChannel6Enabled;
	        return this;
	    }
	    
	    public Builder setLoad6Name(String load6Name) {
			this.load6Name = load6Name;
			return this;
		}

	    public Builder setChannelAddressLoad6(String channelAddressLoad6) {
	        this.channelAddressLoad6 = channelAddressLoad6;
	        return this;
	    }

	    public Builder setChannel6LogicType(LogicType channel6LogicType) {
	        this.channel6LogicType = channel6LogicType;
	        return this;
	    }

	    public Builder setLoad6SwitchedPower(int load6SwitchedPower) {
	        this.load6SwitchedPower = load6SwitchedPower;
	        return this;
	    }
	    
	    // Load 7 Methods
	    public Builder setLoadChannel7Enabled(Boolean loadChannel7Enabled) {
	        this.loadChannel7Enabled = loadChannel7Enabled;
	        return this;
	    }
	    
	    public Builder setLoad7Name(String load7Name) {
			this.load7Name = load7Name;
			return this;
		}

	    public Builder setChannelAddressLoad7(String channelAddressLoad7) {
	        this.channelAddressLoad7 = channelAddressLoad7;
	        return this;
	    }

	    public Builder setChannel7LogicType(LogicType channel7LogicType) {
	        this.channel7LogicType = channel7LogicType;
	        return this;
	    }

	    public Builder setLoad7SwitchedPower(int load7SwitchedPower) {
	        this.load7SwitchedPower = load7SwitchedPower;
	        return this;
	    }
	    
	    // Load 8 Methods
	    public Builder setLoadChannel8Enabled(Boolean loadChannel8Enabled) {
	        this.loadChannel8Enabled = loadChannel8Enabled;
	        return this;
	    }
	    
	    public Builder setLoad8Name(String load8Name) {
			this.load8Name = load8Name;
			return this;
		}

	    public Builder setChannelAddressLoad8(String channelAddressLoad8) {
	        this.channelAddressLoad8 = channelAddressLoad8;
	        return this;
	    }

	    public Builder setChannel8LogicType(LogicType channel8LogicType) {
	        this.channel8LogicType = channel8LogicType;
	        return this;
	    }

	    public Builder setLoad8SwitchedPower(int load8SwitchedPower) {
	        this.load8SwitchedPower = load8SwitchedPower;
	        return this;
	    }
	    
	    // Generator 
	    public Builder setGeneratorProtectionLoadManagementEnabled(Boolean generatorProtectionLoadManagementEnabled) {
	    	this.generatorProtectionLoadManagementEnabled = generatorProtectionLoadManagementEnabled;
	    	return this;
	    }
	    
	    public Builder setChannelAddressGeneratorOn(String channelAddressGeneratorOn) {
	    	this.channelAddressGeneratorOn = channelAddressGeneratorOn;
	    	return this;
	    }
		

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
	public String gridMeterId() {
		return this.builder.gridMeterId;
	}

	@Override
	public int importPowerThreshold() {
		return this.builder.importPowerThreshold;
	}

	@Override
	public int importOverThresholdLimit() {
		return this.builder.importOverThresholdLimit;
	}

	@Override
	public int waitTime() {
		return this.builder.waitTime;
	}

	@Override
	public boolean loadChannel1Enabled() {
		return this.builder.loadChannel1Enabled;
	}
	
	@Override
	public String load1Name() {
		return this.builder.load1Name;
	}

	@Override
	public String channelAddressLoad1() {
		return this.builder.channelAddressLoad1;
	}

	@Override
	public LogicType channel1LogicType() {
		return this.builder.channel1LogicType;
	}

	@Override
	public int load1SwitchedPower() {
		return this.builder.load1SwitchedPower;
	}

	@Override
	public boolean loadChannel2Enabled() {
		return this.builder.loadChannel2Enabled;
	}
	
	@Override
	public String load2Name() {
		return this.builder.load2Name;
	}

	@Override
	public String channelAddressLoad2() {
		return this.builder.channelAddressLoad2;
	}

	@Override
	public LogicType channel2LogicType() {
		return this.builder.channel2LogicType;
	}

	@Override
	public int load2SwitchedPower() {
		return this.builder.load2SwitchedPower;
	}

	@Override
	public boolean loadChannel3Enabled() {
		return this.builder.loadChannel3Enabled;
	}
	
	@Override
	public String load3Name() {
		return this.builder.load3Name;
	}

	@Override
	public String channelAddressLoad3() {
		return this.builder.channelAddressLoad3;
	}

	@Override
	public LogicType channel3LogicType() {
		return this.builder.channel3LogicType;
	}

	@Override
	public int load3SwitchedPower() {
		return this.builder.load3SwitchedPower;
	}

	@Override
	public boolean loadChannel4Enabled() {
		return this.builder.loadChannel4Enabled;
	}
	
	@Override
	public String load4Name() {
		return this.builder.load4Name;
	}

	@Override
	public String channelAddressLoad4() {
		return this.builder.channelAddressLoad4;
	}

	@Override
	public LogicType channel4LogicType() {
		return this.builder.channel4LogicType;
	}

	@Override
	public int load4SwitchedPower() {
		return this.builder.load4SwitchedPower;
	}

	@Override
	public StartupBehavior startupBehaviorPreference() {
		return this.builder.startupBehaviorPreference;
	}

	@Override
	public boolean loadChannel5Enabled() {
		return this.builder.loadChannel5Enabled;
	}

	@Override
	public String load5Name() {
		return this.builder.load5Name;
	}

	@Override
	public String channelAddressLoad5() {
		return this.builder.channelAddressLoad5;
	}

	@Override
	public LogicType channel5LogicType() {
		return this.builder.channel5LogicType;
	}

	@Override
	public int load5SwitchedPower() {
		return this.builder.load5SwitchedPower;
	}

	@Override
	public boolean loadChannel6Enabled() {
		return this.builder.loadChannel6Enabled;
	}

	@Override
	public String load6Name() {
		return this.builder.load6Name;
	}

	@Override
	public String channelAddressLoad6() {
		return this.builder.channelAddressLoad6;
	}

	@Override
	public LogicType channel6LogicType() {
		return this.builder.channel6LogicType;
	}

	@Override
	public int load6SwitchedPower() {
		return this.builder.load6SwitchedPower;
	}

	@Override
	public boolean loadChannel7Enabled() {
		return this.builder.loadChannel7Enabled;
	}

	@Override
	public String load7Name() {
		return this.builder.load7Name;
	}

	@Override
	public String channelAddressLoad7() {
		return this.builder.channelAddressLoad7;
	}

	@Override
	public LogicType channel7LogicType() {
		return this.builder.channel7LogicType;
	}

	@Override
	public int load7SwitchedPower() {
		return this.builder.load7SwitchedPower;
	}

	@Override
	public boolean loadChannel8Enabled() {
		return this.builder.loadChannel8Enabled;
	}

	@Override
	public String load8Name() {
		return this.builder.load8Name;
	}

	@Override
	public String channelAddressLoad8() {
		return this.builder.channelAddressLoad8;
	}

	@Override
	public LogicType channel8LogicType() {
		return this.builder.channel8LogicType;
	}

	@Override
	public int load8SwitchedPower() {
		return this.builder.load8SwitchedPower;
	}

	@Override
	public boolean generatorProtectionLoadManagementEnabled() {
		return this.builder.generatorProtectionLoadManagementEnabled;
	}

	@Override
	public String channelAddressGeneratorOn() {
		return this.builder.channelAddressGeneratorOn;
	}

}