package io.openems.backend.metadata.gridvolt;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name="Metadata.Gridvolt",
		description = "Configures the GridVolt metadata provider.")
public @interface Config {
	
	@AttributeDefinition(name = "Keycloak URL", description = "URL for Keycloak server")
	String keycloakUrl() default "http://localhost";
	
	@AttributeDefinition(name = "Keycloak Port", description = "Port for Keycloak server")
	int keycloakPort() default 8080;
	
	@AttributeDefinition(name = "Keycloak realm", description = "Keycloak realm name")
	String keycloakRealm() default "master";
	
	@AttributeDefinition(name = "Keycloak Client ID", description = "Keycloak client ID")
	String keycloakClientId() default "ems";
	
	@AttributeDefinition(name = "Keycloak Client Secret", description = "Keycloak client secret")
	String keycloakClientSecret() default "";
	
	@AttributeDefinition(name = "Postgres Host", description = "The Postgres host")
	String pgHost() default "localhost";

	@AttributeDefinition(name = "Postgres Port", description = "The Postgres port")
	int pgPort() default 5432;

	@AttributeDefinition(name = "Postgres Username", description = "The Postgres username")
	String pgUser() default "odoo";

	@AttributeDefinition(name = "Postgres Password", description = "The Postgres password")
	String pgPassword();

	@AttributeDefinition(name = "Database", description = "The database name")
	String database();
	
}
