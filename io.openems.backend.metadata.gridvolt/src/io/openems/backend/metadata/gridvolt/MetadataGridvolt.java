package io.openems.backend.metadata.gridvolt;

import java.util.Collection;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//import org.keycloak.representations.AccessTokenResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import io.openems.backend.common.metadata.AbstractMetadata;
import io.openems.backend.common.metadata.AlertingSetting;
import io.openems.backend.common.metadata.Edge;
import io.openems.backend.common.metadata.EdgeHandler;
import io.openems.backend.common.metadata.Metadata;
import io.openems.backend.common.metadata.User;
import io.openems.backend.metadata.gridvolt.keycloak.KeycloakHandler;
import io.openems.backend.metadata.gridvolt.keycloak.TokenResponse;
import io.openems.backend.metadata.gridvolt.Config;
import io.openems.common.OpenemsOEM.Manufacturer;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.jsonrpc.request.GetEdgesRequest.PaginationOptions;
import io.openems.common.jsonrpc.response.GetEdgesResponse.EdgeMetadata;
import io.openems.common.session.Language;

@Designate(ocd = Config.class, factory = false)
@Component(
		name="Metadata.Gridvolt", //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		immediate = true //
)
@EventTopics({ //
	Edge.Events.ALL_EVENTS //
})
public class MetadataGridvolt extends AbstractMetadata implements Metadata, EventHandler {
	
	private final Logger log = LoggerFactory.getLogger(MetadataGridvolt.class);
	
	protected KeycloakHandler keycloakHandler = null;
	
	public MetadataGridvolt() {
		super("Metadata.Gridvolt");
	}
	
	@Activate
	private void activate(Config config) {
		
		this.logInfo(this.log, "Activate. " //
				+ "Keycloak [" + config.keycloakUrl() + ":" + config.keycloakPort() + ";"
				);
		this.keycloakHandler = new KeycloakHandler(this, config);
	}
	
	@Deactivate
	private void deactivate() {
		this.logInfo(this.log, "Deactivate");
	}
	
	@Override
	public User authenticate(String username, String password) throws OpenemsNamedException {
		User user = null;
		TokenResponse tokenResponse = keycloakHandler.authenticate(username, password);
		if (tokenResponse != null) {
			user = this.authenticate(tokenResponse.accessToken);
		}
		return user; // Validate the token and return the user
		//throw new UnsupportedOperationException("MetadataGridvolt.authenticate() is not implemented");
	}
	
	@Override
	public User authenticate(String token) throws OpenemsNamedException {
		// Takes token (JWT) as parameter, validates the token and returns the user obj.
		
		return keycloakHandler.validateToken(token);
	}
	
	@Override
	public void logout(User user) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EdgeHandler edge() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Optional<String> getEdgeIdForApikey(String apikey) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Edge> getEdge(String edgeId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Edge> getEdgeBySetupPassword(String setupPassword) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<User> getUser(String userId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Collection<Edge> getAllOfflineEdges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addEdgeToUser(User user, Edge edge) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getUserInformation(User user) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserInformation(User user, JsonObject jsonObject) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("MetadataGridvolt.setUserInformation() is not implemented");
	}

	@Override
	public byte[] getSetupProtocol(User user, int setupProtocolId) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("MetadataGridvolt.getSetupProtocol() is not implemented");
	}
	
	@Override
	public JsonObject getSetupProtocolData(User user, String edgeId) throws OpenemsNamedException {
		throw new UnsupportedOperationException("MetadataGridvolt.getSetupProtocolData() is not implemented");
	}
	
	@Override
	public int submitSetupProtocol(User user, JsonObject jsonObject) {
		throw new UnsupportedOperationException("MetadataGridvolt.submitSetupProtocol() is not implemented");
	}


	@Override
	public void registerUser(JsonObject user, Manufacturer oem) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("MetadataGridvolt.registerUser() is not implemented");
	}

	@Override
	public void updateUserLanguage(User user, Language language) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("MetadataGridvolt.updateUserLanguage() is not implemented");
	}

	@Override
	public List<AlertingSetting> getUserAlertingSettings(String edgeId) throws OpenemsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlertingSetting getUserAlertingSettings(String edgeId, String userId) throws OpenemsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserAlertingSettings(User user, String edgeId, List<AlertingSetting> users) throws OpenemsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public EventAdmin getEventAdmin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getSerialNumberForEdge(Edge edge) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<EdgeMetadata> getPageDevice(User user, PaginationOptions paginationOptions)
			throws OpenemsNamedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EdgeMetadata getEdgeMetadataForUser(User user, String edgeId) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logGenericSystemLog(GenericSystemLog systemLog) {
		// TODO Auto-generated method stub
		
	}
	
}
