package io.openems.edge.timeofusetariff.swisspower;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Time-Of-Use Tariff Swisspower", //
		description = "Time-Of-Use Tariff implementation for Swisspower.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "timeOfUseTariff0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Access Token", description = "Access token for the Swisspower Platform", type = AttributeType.PASSWORD)
	String accessToken() default "";

	@AttributeDefinition(name = "Measuring point number", description = "Measuring point number for which the tariff is to be retrieved. If this option is used, the tariff name is automatically selected.")
	String meteringCode() default "";

	String webconsole_configurationFactory_nameHint() default "Time-Of-Use Tariff Swisspower [{id}]";
}