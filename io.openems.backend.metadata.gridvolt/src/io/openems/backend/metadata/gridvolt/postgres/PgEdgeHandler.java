package io.openems.backend.metadata.gridvolt.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariDataSource;

import io.openems.backend.common.metadata.Metadata.GenericSystemLog;
import io.openems.backend.common.metadata.User;
import io.openems.backend.metadata.gridvolt.Field;
import io.openems.backend.metadata.gridvolt.Field.EdgeConfigUpdate;
import io.openems.backend.metadata.gridvolt.Field.EdgeDevice;
import io.openems.backend.metadata.gridvolt.Field.EdgeDeviceUserRole;
import io.openems.common.channel.Level;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.jsonrpc.request.GetEdgesRequest.PaginationOptions;
import io.openems.common.jsonrpc.response.GetEdgesResponse.EdgeMetadata;
import io.openems.common.session.Role;
import io.openems.common.types.EdgeConfig;
import io.openems.common.types.EdgeConfig.Component.JsonFormat;
import io.openems.common.types.EdgeConfigDiff;
import io.openems.common.types.SemanticVersion;
import io.openems.common.utils.JsonUtils;

public final class PgEdgeHandler {

	private final HikariDataSource dataSource;
	private final Logger log = LoggerFactory.getLogger(PgEdgeHandler.class);


	protected PgEdgeHandler(HikariDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Gets the {@link EdgeConfig} for an Edge-ID.
	 * 
	 * @param edgeId the Edge-ID
	 * @return the {@link EdgeConfig}
	 * @throws OpenemsNamedException on error
	 * @throws SQLException          on error
	 */
	public EdgeConfig getEdgeConfig(String edgeId) throws SQLException, OpenemsNamedException {
		
		String sql = new StringBuilder()
				.append("SELECT ").append(EdgeDevice.OPENEMS_CONFIG.id())
				.append(" FROM ").append(EdgeDevice.DB_TABLE)
				.append(" WHERE ").append(EdgeDevice.ID.id()).append(" = ?")
				.append(" LIMIT 1;")
				.toString();
		
		try (var con = this.dataSource.getConnection(); //
				var pst = con.prepareStatement(sql)) {
			pst.setString(1, edgeId);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					var string = rs.getString(EdgeDevice.OPENEMS_CONFIG.id());
					if (string == null) {
						throw new OpenemsException("EdgeConfig for [" + edgeId + "] is null in the Database");
					}
					return EdgeConfig.fromJson(//
							JsonUtils.parseToJsonObject(string));
				}
			}
		}
		throw new OpenemsException("Unable to find EdgeConfig for [" + edgeId + "]");
	}

	/**
	 * Updates the {@link EdgeConfig} for an Edge-ID.
	 * 
	 * @param edgeId     the Edge-ID
	 * @param edgeConfig the {@link EdgeConfig}
	 * @throws OpenemsNamedException on error
	 * @throws SQLException          on error
	 */
	public void updateEdgeConfig(String edgeId, EdgeConfig edgeConfig) throws SQLException, OpenemsNamedException {
		String sql = new StringBuilder() //
				.append("UPDATE ").append(EdgeDevice.DB_TABLE) //
				.append(" SET ") //
				.append(EdgeDevice.OPENEMS_CONFIG.id()).append(" = ?, ") //
				.append(EdgeDevice.OPENEMS_CONFIG_COMPONENTS.id()).append(" = ?") //
				.append(" WHERE id = ?") //
				.toString();
		
		try (var con = this.dataSource.getConnection(); //
				var pst = con.prepareStatement(sql)) {
			pst.setString(1, JsonUtils.prettyToString(edgeConfig.toJson()));
			pst.setString(2, JsonUtils.prettyToString(edgeConfig.componentsToJson(JsonFormat.WITHOUT_CHANNELS)));
			pst.setString(3, edgeId);
			pst.execute();
		}
	}

	/**
	 * Inserts an {@link EdgeConfigDiff} for an Edge-ID.
	 * 
	 * @param id the Edge-ID
	 * @param diff   the {@link EdgeConfigDiff}
	 * @throws OpenemsNamedException on error
	 * @throws SQLException          on error
	 */
	public void insertEdgeConfigUpdate(String id, EdgeConfigDiff diff) throws SQLException, OpenemsNamedException {
		String sql = new StringBuilder() //
				.append("INSERT INTO ").append(EdgeConfigUpdate.DB_TABLE) //
				.append(" (create_date") //
				.append(", ").append(EdgeConfigUpdate.DEVICE_ID.id()) //
				.append(", ").append(EdgeConfigUpdate.TEASER.id()) //
				.append(", ").append(EdgeConfigUpdate.DETAILS.id()) //
				.append(") VALUES(?, ?, ?, ?)") //
				.toString();
		
		try (var con = this.dataSource.getConnection(); //
				var pst = con.prepareStatement(sql)) {
			pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
			pst.setString(2, id);
			pst.setString(3, diff.getAsText());
			pst.setString(4, diff.getAsHtml());
			pst.execute();
		}
	}

	/**
	 * Inserts an {@link GenericSystemLog} for an Edge-ID.
	 * 
	 * @param id    the Edge-ID
	 * @param systemLog the {@link GenericSystemLog}
	 * @throws OpenemsNamedException on error
	 * @throws SQLException          on error
	 */
	public void insertGenericSystemLog(String id, GenericSystemLog systemLog)
			throws SQLException, OpenemsNamedException {
		
		String sql = new StringBuilder() //
				.append("INSERT INTO ").append(EdgeConfigUpdate.DB_TABLE) //
				.append(" (create_date") //
				.append(", ").append(EdgeConfigUpdate.DEVICE_ID.id()) //
				.append(", ").append(EdgeConfigUpdate.TEASER.id()) //
				.append(", ").append(EdgeConfigUpdate.DETAILS.id()) //
				.append(") VALUES(?, ?, ?, ?)") //
				.toString();
		
		try (var con = this.dataSource.getConnection(); //
				var pst = con.prepareStatement(sql)) {
			pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
			pst.setString(2, id);
			pst.setString(3, systemLog.teaser());
			pst.setString(4, createHtml(systemLog));
			pst.execute();
		}
	}
	
	/**
	 * Created an entry in the EdgeDeviceUserRole table connecting the device and user
	 * @param userId
	 * @param edgeId
	 * @throws SQLException
	 */
	public void assignEdgeToUser(String userId, String edgeId) throws SQLException {
		String sql = new StringBuilder()
                .append("INSERT INTO ").append(EdgeDeviceUserRole.DB_TABLE)
                .append(" (create_date")
                .append(", ").append(EdgeDeviceUserRole.ID.id())
                .append(", ").append(EdgeDeviceUserRole.EDGE_ID.id())
                .append(", ").append(EdgeDeviceUserRole.USERID_KEYCLOAK.id())
                .append(") VALUES(?, ?, ?, ?)")
                .toString();

		try (Connection con = this.dataSource.getConnection();
		     PreparedStatement pst = con.prepareStatement(sql)) {
		    pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC)));
		    pst.setString(2, UUID.randomUUID().toString());
		    pst.setString(3, edgeId);
		    pst.setString(4, userId);
		    pst.execute();
		}
	}
	
	/**
	 * Retrieves a list of edges for a given user in the EdgeMetadata format. The paginationOptions allow the app to select for particular isOnline, sumState etc values
	 * @param user
	 * @param paginationOptions 
	 * @return
	 * @throws SQLException
	 * @throws OpenemsException
	 */
	public List<EdgeMetadata> getEdges(User user, PaginationOptions paginationOptions) throws SQLException, OpenemsException {
		// This function executes a JOIN across EdgeDeviceUserRole and EdgeDevice tables
		// It uses a relatively complex query to SELECT the EdgeDeviceUserRole entries which match the user ID, and then JOINs to retrieve the matching entries in the edges table 
		
		String userId = user.getId();
		List<EdgeMetadata> edges = new ArrayList<>();
		
		log.debug("getEdges called");
		
		StringBuilder sql = new StringBuilder()
		        .append("SELECT ")
		        .append(EdgeDeviceUserRole.DB_TABLE).append(".").append(EdgeDeviceUserRole.EDGE_ID.id()).append(", ")
		        .append(EdgeDeviceUserRole.DB_TABLE).append(".").append(EdgeDeviceUserRole.ROLE.id()).append(", ")
		        .append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.LASTMESSAGE.id()).append(", ")
		        .append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.PRODUCTTYPE.id()).append(", ")
		        .append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.OPENEMS_IS_CONNECTED.id()).append(", ")
		        .append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.COMMENT.id()).append(", ")
		        .append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.OPENEMS_SUM_STATE.id()).append(", ")
		        .append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.OPENEMS_VERSION.id())
		        .append(" FROM ")
		        .append(EdgeDeviceUserRole.DB_TABLE).append(" ")
		        .append("INNER JOIN ")
		        .append(EdgeDevice.DB_TABLE).append(" ON ").append(EdgeDeviceUserRole.DB_TABLE).append(".").append(EdgeDeviceUserRole.EDGE_ID.id()).append(" = ").append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.ID.id())
		        .append(" WHERE ").append(EdgeDeviceUserRole.USERID_KEYCLOAK.id()).append(" = ? ");
		 
		// Modifying the query to look for the search params
		if (paginationOptions.getSearchParams() != null) {
			
			// If productTypes exists we add all productTypes in a (?,?,?) tuple
			// Need to be careful when adding the relevant values to the query later as the number of values is variable.
			List<String> productTypes = paginationOptions.getSearchParams().productTypes();
			if (productTypes != null && !productTypes.isEmpty()) {
				 String inSql = String.join(",", Collections.nCopies(productTypes.size(), "?"));
				 sql.append(" AND ").append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.PRODUCTTYPE.id()).append(" IN (" + inSql + ")");
			}
			
			List<Level> sumStates = paginationOptions.getSearchParams().sumStates();
			if (sumStates != null && !sumStates.isEmpty()) {
				String inSql = String.join(",", Collections.nCopies(sumStates.size(), "?"));
				sql.append(" AND ").append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.OPENEMS_SUM_STATE.id()).append(" IN (" + inSql + ")");
			}
			
			// I imaging that searchIsOnline determines whether to use the isOnline parameter in the search. If not true we don't add any term for the inOnline column.
			boolean searchIsOnline = paginationOptions.getSearchParams().searchIsOnline();
			if (searchIsOnline == true) {
				sql.append(" AND ").append(EdgeDevice.DB_TABLE).append(".").append(EdgeDevice.OPENEMS_IS_CONNECTED.id()).append(" = ? ");
			}
			 
			 
		 }

		// Add pagination
		int limit = paginationOptions.getLimit();
		int offset = paginationOptions.getPage() * limit;
		sql.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
		
		try (Connection con = this.dataSource.getConnection();
			     PreparedStatement pst = con.prepareStatement(sql.toString())) {
			
			Integer i = 1; // The current index within the prepared statement
			pst.setString(i++, userId);
			
			if (paginationOptions.getSearchParams() != null) {
				
				List<String> productTypes = paginationOptions.getSearchParams().productTypes();
				if (productTypes != null && !productTypes.isEmpty()) {
					for (String item : productTypes) {
						pst.setString(i++, item);
					}
				}
				
				List<Level> sumStates = paginationOptions.getSearchParams().sumStates();
				if (sumStates != null && !sumStates.isEmpty()) {
					for (Level item : sumStates) {
						pst.setString(i++, item.toString());
					}
				}
				
				boolean searchIsOnline = paginationOptions.getSearchParams().searchIsOnline();
				boolean isOnline = paginationOptions.getSearchParams().isOnline();
				if (searchIsOnline == true) {
					pst.setBoolean(i++, isOnline);
				}
								
			}
			
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
										
					String edgeId = rs.getString(EdgeDeviceUserRole.EDGE_ID.id());
	                String roleStr = rs.getString(EdgeDeviceUserRole.ROLE.id());
	                String comment = rs.getString(EdgeDevice.COMMENT.id());
		    		String producttype = rs.getString( EdgeDevice.PRODUCTTYPE.id());
		    		SemanticVersion version = SemanticVersion.fromString(rs.getString(EdgeDevice.OPENEMS_VERSION.id()));
		    		boolean isOnline = rs.getBoolean(EdgeDevice.OPENEMS_IS_CONNECTED.id());
		    		ZonedDateTime lastMessage = rs.getObject(EdgeDevice.LASTMESSAGE.id(), OffsetDateTime.class).toZonedDateTime();
		    		Level sumState = this.getLevelFromString(rs.getString(EdgeDevice.OPENEMS_SUM_STATE.id()));
	                
	                Role role = Role.getRole(roleStr);
	                
	                EdgeMetadata edgeMetadata = new EdgeMetadata(edgeId, comment, producttype, version, role, isOnline, lastMessage, null, sumState);
	                edges.add(edgeMetadata);
	                	
				}
			} catch (OpenemsException e) {
				e.printStackTrace();
				throw e;
			}
		}
		
		return edges;
	}
	
	public EdgeMetadata getEdgeMetadata(String edgeId, Role role) throws SQLException, OpenemsException {
		
		String sql = new StringBuilder()
				.append("SELECT ")
				.append(EdgeDevice.ID.id()).append(", ")
				.append(EdgeDevice.COMMENT.id()).append(", ")
				.append(EdgeDevice.PRODUCTTYPE.id()).append(", ")
				.append(EdgeDevice.OPENEMS_VERSION.id()).append(", ")
				.append(EdgeDevice.OPENEMS_IS_CONNECTED.id()).append(", ")
				.append(EdgeDevice.LASTMESSAGE.id()).append(", ")
				.append(EdgeDevice.OPENEMS_SUM_STATE.id())
				.append(" FROM ").append(EdgeDevice.DB_TABLE)
				.append(" WHERE ").append(EdgeDevice.ID.id()).append(" = ?")
				.append(" LIMIT 1;")
				.toString();
		
		try (Connection con = this.dataSource.getConnection();
			     PreparedStatement pst = con.prepareStatement(sql)) {
			    pst.setString(1, edgeId);
			    try (ResultSet rs = pst.executeQuery()) {
			    	
			    	while (rs.next()) {
			    		String comment = rs.getString(EdgeDevice.COMMENT.id());
			    		String producttype = rs.getString(EdgeDevice.PRODUCTTYPE.id());
			    		SemanticVersion version = SemanticVersion.fromString(rs.getString(EdgeDevice.OPENEMS_VERSION.id()));
			    		boolean isOnline = rs.getBoolean(EdgeDevice.OPENEMS_IS_CONNECTED.id());
			    		ZonedDateTime lastMessage = rs.getObject(EdgeDevice.LASTMESSAGE.id(), OffsetDateTime.class).toZonedDateTime();
			    		Level sumState = this.getLevelFromString(rs.getString(EdgeDevice.OPENEMS_SUM_STATE.id()));
			    		EdgeMetadata edgeMetadata = new EdgeMetadata(edgeId, comment, producttype, version, role, isOnline, lastMessage, null, sumState);
			    		return edgeMetadata;
			    	}
			    }
			}
		
		throw new OpenemsException("Error while fetching edge metadata for edge " + edgeId);
	}
	
	private Level getLevelFromString(String levelStr) throws OpenemsException {
	    String lowerCaseLevelStr = levelStr.toLowerCase();

	    if (lowerCaseLevelStr.equals("ok")) {
	        return Level.OK;
	    } 
	    else if (lowerCaseLevelStr.equals("info")) {
	        return Level.INFO;
	    }
	    else if (lowerCaseLevelStr.equals("warning")) {
	        return Level.WARNING;
	    }
	    else if (lowerCaseLevelStr.equals("fault")) {
	        return Level.FAULT;
	    }
	    throw new OpenemsException("Invalid Sum-state level string.");
	}
	
	public EdgeMetadata getEdgeWithRole(User user, String edgeId) throws SQLException, OpenemsException {
		// Check that user role exists for this edge - then get the role
		Role role = null;
		EdgeMetadata edgeMetadata = null;
		
		String sql1 = new StringBuilder()
				.append("SELECT ")
				.append(EdgeDeviceUserRole.ID.id()).append(", ")
				.append(EdgeDeviceUserRole.ROLE.id()).append(", ")
				.append(" FROM ").append(EdgeDeviceUserRole.DB_TABLE)
				.append(" WHERE ")
				.append(EdgeDeviceUserRole.USERID_KEYCLOAK.id()).append(" = ?")
				.append(" AND ")
				.append(EdgeDeviceUserRole.EDGE_ID.id()).append(" = ?")
				.toString();
		
		try (Connection con = this.dataSource.getConnection();
			     PreparedStatement pst = con.prepareStatement(sql1)) {
			pst.setString(1, user.getId());
			pst.setString(2, edgeId);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					role = Role.getRole(rs.getString(EdgeDeviceUserRole.ROLE.id()));
					
				}
			}
		}
		
		if (role != null) {
			edgeMetadata = this.getEdgeMetadata(edgeId, role);
			return edgeMetadata;
		}
		throw new OpenemsException("Unable to find user role for user " + user.getId() + " in edge " + edgeId);
		// Then get EdgeMetadata
	}

	private static String createHtml(GenericSystemLog systemLog) {
		final var header = new StringBuilder();
		final var content = new StringBuilder();

		header.append("""
				<table border="1" style="border-collapse: collapse"\
				<thead>\
					<tr>""");
		content.append("<tr>");
		for (var entry : systemLog.getValues().entrySet()) {
			header.append("<th>%s</th>".formatted(entry.getKey()));
			content.append("<td>%s</td>".formatted(entry.getValue()));
		}
		header.append("<th>Executed By</th>");
		content.append("<td>%s: %s</td>".formatted(systemLog.user().getId(), systemLog.user().getName()));

		header.append("""
					</tr>
				</thead>
				<tbody>
				""");
		content.append("</tr>");

		return new StringBuilder() //
				.append(header) //
				.append(content) //
				.append("</tbody></table>") //
				.toString();
	}

	/**
	 * Updates the ProductType for an Edge-ID.
	 * 
	 * @param id      the Edge-ID
	 * @param producttype the ProductType
	 * @throws OpenemsNamedException on error
	 * @throws SQLException          on error
	 */
	public void updateProductType(String id, String producttype) throws SQLException, OpenemsNamedException {
		try (var con = this.dataSource.getConnection(); //
				var pst = con.prepareStatement(new StringBuilder() //
						.append("UPDATE ").append(EdgeDevice.DB_TABLE) //
						.append(" SET ") //
						.append(EdgeDevice.PRODUCTTYPE.id()).append(" = ?") //
						.append(" WHERE id = ?") //
						.toString())) {
			pst.setString(1, producttype);
			pst.setString(2, id);
			pst.execute();
		}
	}
	
	public HashMap<String, Role> getEdgeRolesForUser(User user) throws SQLException {
		
		HashMap<String, Role> roles = new HashMap<>();
		
		String sql = new StringBuilder()
				.append("SELECT ")
				.append(EdgeDeviceUserRole.EDGE_ID.id()).append(", ")
				.append(EdgeDeviceUserRole.ROLE.id())
				.append(" FROM ").append(EdgeDeviceUserRole.DB_TABLE)
				.append(" WHERE ").append(EdgeDeviceUserRole.USERID_KEYCLOAK.id()).append(" = ?")
				.toString();
		
		try (Connection con = this.dataSource.getConnection();
			     PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, user.getId());
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					String edgeId = rs.getString(EdgeDeviceUserRole.EDGE_ID.id());
	                String roleStr = rs.getString(EdgeDeviceUserRole.ROLE.id());
	                
	                Role role = Role.getRole(roleStr);
	                
	                roles.put(edgeId, role);
				}
			}
		}
		return roles;
	}

	/**
	 * Updates the OpenemsIsConnected field for multiple Edge-IDs.
	 * 
	 * @param edgeIds     the Edge-IDs
	 * @param isConnected true if online; false if offline
	 * @throws SQLException on error
	 */
	public void updateOpenemsIsConnected(Set<String> edgeIds, boolean isConnected) throws SQLException {
		log.info("Updating isOnline for " + edgeIds.toString() + " " + isConnected);
		if (edgeIds.isEmpty()) {
			return;
		}

		try (var con = this.dataSource.getConnection(); //
				var st = con.createStatement()) {
			st.executeUpdate(new StringBuilder() //
					.append("UPDATE ").append(EdgeDevice.DB_TABLE) //
					.append(" SET ").append(Field.EdgeDevice.OPENEMS_IS_CONNECTED.id()).append(" = ")
					.append(isConnected ? "TRUE" : "FALSE") //
					.append(" WHERE id IN (") //
					.append(edgeIds.stream()
			                .map(id -> "'" + id.replace("'", "''") + "'") // Properly quote and escape each ID
			                .collect(Collectors.joining(",")))
					.append(")") //
					.toString());
		}
	}

	/**
	 * Updates the LastMessage field for multiple Edge-IDs.
	 * 
	 * @param edgeIds the Edge-IDs
	 * @throws OpenemsNamedException on error
	 * @throws SQLException          on error
	 */
	public void updateLastMessage(Set<String> edgeIds) throws SQLException {
		if (edgeIds.isEmpty()) {
			return;
		}

		try (var con = this.dataSource.getConnection(); //
				var st = con.createStatement()) {
			st.executeUpdate(new StringBuilder() //
					.append("UPDATE ").append(EdgeDevice.DB_TABLE) //
					.append(" SET ").append(Field.EdgeDevice.LASTMESSAGE.id()).append(" = (now() at time zone 'UTC')") //
					.append(" WHERE id IN (") //
					.append(edgeIds.stream()
			                .map(id -> "'" + id.replace("'", "''") + "'") // Properly quote and escape each ID
			                .collect(Collectors.joining(",")))
					.append(")") //
					.toString());
		}
	}
	
	/**
	 * Updates the version in the DB
	 * @param id
	 * @param version
	 * @throws SQLException
	 */
	public void updateVersion(String id, SemanticVersion version) throws SQLException {
		
		try (var con = this.dataSource.getConnection(); //
				var pst = con.prepareStatement(new StringBuilder() //
						.append("UPDATE ").append(EdgeDevice.DB_TABLE) //
						.append(" SET ") //
						.append(EdgeDevice.OPENEMS_VERSION.id()).append(" = ?") //
						.append(" WHERE id = ?") //
						.toString())) {
			pst.setString(1, version.toString());
			pst.setString(2, id);
			pst.execute();
		}

	}


	/**
	 * Updates the Sum-State field for multiple Edge-IDs.
	 * 
	 * @param edgeIds the Edge-IDs
	 * @param level   the Sum-State {@link Level}
	 * @throws OpenemsNamedException on error
	 * @throws SQLException          on error
	 */
	public void updateSumState(Set<String> edgeIds, Level level) throws SQLException {
		if (edgeIds.isEmpty()) {
			return;
		}

		try (var con = this.dataSource.getConnection(); //
				var st = con.createStatement()) {
			st.executeUpdate(new StringBuilder() //
					.append("UPDATE ").append(EdgeDevice.DB_TABLE) //
					.append(" SET ").append(Field.EdgeDevice.OPENEMS_SUM_STATE.id()).append(" = '")
					.append(level.getName().toLowerCase()) //
					.append("' WHERE id IN (") //
					.append(edgeIds.stream()
			                .map(id -> "'" + id.replace("'", "''") + "'") // Properly quote and escape each ID
			                .collect(Collectors.joining(",")))
					.append(")") //
					.toString());
		}
	}
}