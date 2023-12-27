package io.openems.backend.metadata.gridvolt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonObject;

import io.openems.backend.common.metadata.AbstractMetadata;
import io.openems.backend.common.metadata.AlertingSetting;
import io.openems.backend.common.metadata.Edge;
import io.openems.backend.common.metadata.EdgeHandler;
import io.openems.backend.common.metadata.Metadata;
import io.openems.backend.common.metadata.User;
import io.openems.backend.metadata.gridvolt.keycloak.KeycloakHandler;
import io.openems.backend.metadata.gridvolt.keycloak.TokenResponse;
import io.openems.backend.metadata.gridvolt.postgres.PostgresHandler;
import io.openems.common.OpenemsOEM.Manufacturer;
import io.openems.common.channel.Level;
import io.openems.common.event.EventReader;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.jsonrpc.request.GetEdgesRequest.PaginationOptions;
import io.openems.common.jsonrpc.response.GetEdgesResponse.EdgeMetadata;
import io.openems.common.session.Language;
import io.openems.common.types.EdgeConfig;
import io.openems.common.types.EdgeConfigDiff;
import io.openems.common.types.SemanticVersion;
import io.openems.common.utils.ThreadPoolUtils;

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
	
	private static final int EXECUTOR_MIN_THREADS = 1;
	private static final int EXECUTOR_MAX_THREADS = 50;
	
	private final Logger log = LoggerFactory.getLogger(MetadataGridvolt.class);
	private final EdgeCache edgeCache;
	private final GridvoltEdgeHandler edgeHandler = new GridvoltEdgeHandler(this);
	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(EXECUTOR_MIN_THREADS, EXECUTOR_MAX_THREADS, 60L,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
			new ThreadFactoryBuilder().setNameFormat("Metadata.Gridvolt.Worker-%d").build());
	
	/** Maps User-ID to {@link User}. */
	private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
	
	/** Maps UserID to refreshTokens */
	private final ConcurrentHashMap<String, String> refreshTokens = new ConcurrentHashMap<>();

	@Reference
	private EventAdmin eventAdmin;
	
	protected KeycloakHandler keycloakHandler = null;
	protected PostgresHandler postgresHandler = null;
	
	public MetadataGridvolt() {
		super("Metadata.Gridvolt");
		
		this.edgeCache = new EdgeCache(this);
	}
	
	/**
	 * Gets the {@link PostgresHandler}.
	 *
	 * @return the {@link PostgresHandler}
	 */
	public PostgresHandler getPostgresHandler() {
		return this.postgresHandler;
	}
	
	@Activate
	private void activate(Config config) throws SQLException {
		
		this.logInfo(this.log, "Activate. " //
				+ "Keycloak [" + config.keycloakUrl() + ":" + config.keycloakPort() + ";"
				);
		this.keycloakHandler = new KeycloakHandler(this, config);
		this.postgresHandler = new PostgresHandler(this, this.edgeCache, config, () -> {
			this.setInitialized();
		});
	}
	
	@Deactivate
	private void deactivate() {
		this.logInfo(this.log, "Deactivate");
		ThreadPoolUtils.shutdownAndAwaitTermination(this.executor, 5);
		if (this.postgresHandler != null) {
			this.postgresHandler.deactivate();
		}
	}
	
	@Override
	public User authenticate(String username, String password) throws OpenemsNamedException {
		User user = null;
		TokenResponse tokenResponse = keycloakHandler.authenticate(username, password);
		if (tokenResponse != null) {
			user = this.authenticate(tokenResponse.accessToken);
			this.refreshTokens.put(user.getId(), tokenResponse.refreshToken);
			this.users.put(user.getId(), user);
		}
		log.info("The user role is: " + user.getGlobalRole());
		return user; // Validate the token and return the user
	}
	
	@Override
	public User authenticate(String token) throws OpenemsNamedException {
		// Takes token (JWT) as parameter, validates the token and returns the user obj.
		
		User user = keycloakHandler.validateToken(token);
		
		return user;
	}
	
	@Override
	public void logout(User user) {
		String refreshToken = this.refreshTokens.get(user.getId());
		this.users.remove(user.getId());
		keycloakHandler.logout(user, refreshToken);
		
	}
	
	@Override
	public void handleEvent(Event event) {
		var reader = new EventReader(event);

		switch (event.getTopic()) {
		case Edge.Events.ON_SET_ONLINE: {
			var edgeId = reader.getString(Edge.Events.OnSetOnline.EDGE_ID);
			var isOnline = reader.getBoolean(Edge.Events.OnSetOnline.IS_ONLINE);

			this.getEdge(edgeId).ifPresent(edge -> {
				if (edge instanceof MyEdge) {
					// Set OpenEMS Is Connected in Postgres
					this.postgresHandler.getPeriodicWriteWorker().onSetOnline((MyEdge) edge, isOnline);
				}
			});
		}
			break;

		case Edge.Events.ON_SET_CONFIG:
			this.onSetConfigEvent(reader); // TODO: Implement this
			break;

		case Edge.Events.ON_SET_VERSION: {
			var edge = (MyEdge) reader.getProperty(Edge.Events.OnSetVersion.EDGE);
			var version = (SemanticVersion) reader.getProperty(Edge.Events.OnSetVersion.VERSION);
			
			this.executor.execute(() -> {
				try {
					this.postgresHandler.edge.updateVersion(edge.getId(), version);
				} catch (SQLException e) {
					this.logWarn(this.log, "Edge [" + edge.getId() + "] " //
							+ "Unable to insert update Product Type: " + e.getMessage());
				}
			});
		}
			break;

		case Edge.Events.ON_SET_LASTMESSAGE: {
			var edge = (MyEdge) reader.getProperty(Edge.Events.OnSetLastmessage.EDGE);
			// Set LastMessage timestamp in Postgres
			this.postgresHandler.getPeriodicWriteWorker().onLastMessage(edge);
		}
			break;

		case Edge.Events.ON_SET_SUM_STATE: {
			var edge = (MyEdge) reader.getProperty(Edge.Events.OnSetSumState.EDGE);
			var sumState = (Level) reader.getProperty(Edge.Events.OnSetSumState.SUM_STATE);
			// Set Sum-State in Postgres
			this.postgresHandler.getPeriodicWriteWorker().onSetSumState(edge, sumState);
		}
			break;

		case Edge.Events.ON_SET_PRODUCTTYPE: {
			var edge = (MyEdge) reader.getProperty(Edge.Events.OnSetProducttype.EDGE);
			var producttype = reader.getString(Edge.Events.OnSetProducttype.PRODUCTTYPE);
			// Set Producttype Postgres
			this.executor.execute(() -> {
				try {
					this.postgresHandler.edge.updateProductType(edge.getId(), producttype);
				} catch (SQLException | OpenemsNamedException e) {
					this.logWarn(this.log, "Edge [" + edge.getId() + "] " //
							+ "Unable to insert update Product Type: " + e.getMessage());
				}
			});
		}
			break;

		}
	}
	
	@Override
	public Optional<String> getEdgeIdForApikey(String apikey) {
		var edgeOpt = this.postgresHandler.getEdgeForApikey(apikey);
		if (edgeOpt.isPresent()) {
			return Optional.of(edgeOpt.get().getId());
		}
		return Optional.empty();
	}

	@Override
	public Optional<Edge> getEdge(String edgeId) {
		return Optional.ofNullable(this.edgeCache.getEdgeFromEdgeId(edgeId));
	}

	@Override
	public Optional<Edge> getEdgeBySetupPassword(String setupPassword) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<User> getUser(String userId) {
		return Optional.ofNullable(this.users.get(userId));	
	}

	@Override
	public Collection<Edge> getAllOfflineEdges() {
		return this.edgeCache.getAllEdges().stream().filter(Edge::isOffline).toList();
	}
	

	@Override
	public void addEdgeToUser(User user, Edge edge) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		try {
			this.postgresHandler.edge.assignEdgeToUser(edge.getId(), user.getId());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OpenemsNamedException(OpenemsError.GENERIC, e.getMessage());
		}
				
	}

	@Override
	public Map<String, Object> getUserInformation(User user) throws OpenemsNamedException {
		try {
			return this.postgresHandler.user.getUserInformation(user);
		} catch(SQLException e) {
			throw new OpenemsException(e.getMessage());
		}
	}

	@Override
	public void setUserInformation(User user, JsonObject jsonObject) throws OpenemsNamedException {
		try {
			this.postgresHandler.user.setUserInformation(user, jsonObject);
		} catch (SQLException e) {
			throw new OpenemsException(e.getMessage());
		}
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
		try {
			this.log.info("Language: " + language.toString());
			this.postgresHandler.user.updateUserLanguage(user, language);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new OpenemsException(e.getMessage());
		}
		
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
		return this.eventAdmin;
	}

	@Override
	public Optional<String> getSerialNumberForEdge(Edge edge) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<EdgeMetadata> getPageDevice(User user, PaginationOptions paginationOptions)
			throws OpenemsNamedException {
		List<EdgeMetadata> edges = new ArrayList<>();
		try {
			edges = this.postgresHandler.edge.getEdges(user, paginationOptions);
			return edges;
		} catch (SQLException e) {
			this.log.error(e.toString());
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Exception while getting edges for user " + user.getId());
		}
		throw new OpenemsNamedException(OpenemsError.GENERIC, "Unable to fetch edges for user: " + user.getId());
	}

	@Override
	public EdgeMetadata getEdgeMetadataForUser(User user, String edgeId) throws OpenemsNamedException {
		// TODO Auto-generated method stub
		
		try {
			return this.postgresHandler.edge.getEdgeMetadata(edgeId, user.getGlobalRole());
		} catch (OpenemsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new OpenemsNamedException(OpenemsError.GENERIC, "Unable to fetch edge metadata.");
	}

	@Override
	public void logGenericSystemLog(GenericSystemLog systemLog) {
		this.executor.execute(() -> {
			try {
				Optional<Edge> edge = this.getEdge(systemLog.edgeId());
				if (edge.isPresent()) {
					this.postgresHandler.edge.insertGenericSystemLog(edge.get().getId(), systemLog);
				}
			} catch (SQLException | OpenemsNamedException e) {
				this.log.warn("Unable to insert log to edge");
			}
		});
		
	}
	
	@Override
	public void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	public void logWarn(Logger log, String message) {
		super.logWarn(log, message);
	}

	@Override
	public void logError(Logger log, String message) {
		super.logError(log, message);
	}
	
	
	
	private void onSetConfigEvent(EventReader reader) {
		this.executor.execute(() -> {
			var edge = (MyEdge) reader.getProperty(Edge.Events.OnSetConfig.EDGE);
			var newConfig = (EdgeConfig) reader.getProperty(Edge.Events.OnSetConfig.CONFIG);

			EdgeConfig oldConfig;
			try {
				oldConfig = this.postgresHandler.edge.getEdgeConfig(edge.getId());
			} catch (OpenemsNamedException | SQLException e) {
				oldConfig = EdgeConfig.empty();
				this.logWarn(this.log, "Edge [" + edge.getId() + "]. " + e.getMessage());
			}

			var diff = EdgeConfigDiff.diff(newConfig, oldConfig);
			if (diff.isDifferent()) {
				// Update "EdgeConfigUpdate"
				var diffString = diff.toString();
				if (!diffString.isBlank()) {
					this.logInfo(this.log, "Edge [" + edge.getId() + "]. Update config: " + diff.toString());
				}

				try {
					this.postgresHandler.edge.insertEdgeConfigUpdate(edge.getId(), diff);
				} catch (SQLException | OpenemsNamedException e) {
					this.logWarn(this.log, "Edge [" + edge.getId() + "] " //
							+ "Unable to insert EdgeConfigUpdate: " + e.getMessage());
				}
			}

			// Always update EdgeConfig, because it also updates "openems_config_components"
			try {
				this.postgresHandler.edge.updateEdgeConfig(edge.getId(), newConfig);
			} catch (SQLException | OpenemsNamedException e) {
				this.logWarn(this.log, "Edge [" + edge.getId() + "] " //
						+ "Unable to insert EdgeConfigUpdate: " + e.getMessage());
			}
		});
	}

	@Override
	public EdgeHandler edge() {
		return this.edgeHandler;
	}

	
}
