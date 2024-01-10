package io.openems.backend.metadata.gridvolt.postgres;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariDataSource;

import io.openems.backend.common.metadata.User;
import io.openems.backend.metadata.gridvolt.Field.DbUser;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.session.Language;
import io.openems.common.session.Role;

public final class PgUserHandler {
	
	private final HikariDataSource dataSource;
	
	private final Logger log = LoggerFactory.getLogger(PgUserHandler.class);
		
	protected PgUserHandler(HikariDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * Fetches a user from the DB. We provide it with the username and the access_token and it returns a User object
	 * @param username
	 * @param token
	 * @return
	 */
	
	/**
	 * 
	 * @param id
	 * @param username
	 * @param token
	 * @return
	 * @throws SQLException
	 * @throws OpenemsNamedException
	 */
	public User getUser(String id, String username, String token) throws SQLException, OpenemsNamedException {
		try (var con = this.dataSource.getConnection();
				var pst = con.prepareStatement(new StringBuilder() //
						.append("SELECT * ")
						.append(" FROM ").append(DbUser.DB_TABLE)
						.append(" WHERE ").append(DbUser.KEYCLOAK_ID.id())
						.append(" = ?") //
						.append(" LIMIT 1;")
						.toString())) {
			pst.setString(1, id);
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					String name = rs.getString(DbUser.FIRST_NAME.id()) + " " + rs.getString(DbUser.LAST_NAME.id());
					User user = new User(id, name, token, Language.EN, Role.ADMIN, true);
					return user;
				}
			}
		}
		throw new OpenemsException("Unable to fetch User.");
		
	}
	
	public User getUserForId(String id) {
		String sql = new StringBuilder()
				.toString();
		
		try (Connection con = this.dataSource.getConnection();
				PreparedStatement pst = con.prepareStatement(sql)) {
			
		}
		
		return new User();
	}
	
	/**
	 * Retrieves user Information from the DB. Returns the user details as a JSON object where the keys match the DB headers.
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Object> getUserInformation(User user) throws SQLException {
		HashMap<String, Object> userInformation = new HashMap<>();
		
		
		String sql = new StringBuilder()
				.append("SELECT ")
				.append(DbUser.FIRST_NAME.id()).append(", ")
				.append(DbUser.LAST_NAME.id()).append(", ")
				.append(DbUser.EMAIL.id()).append(", ")
				.append(DbUser.PHONE.id()).append(", ")
				.append(DbUser.STREET.id()).append(", ")
				.append(DbUser.CITY.id()).append(", ")
				.append(DbUser.ZIP.id()).append(", ")
				.append(DbUser.COUNTRY.id())
				.append(" FROM ").append(DbUser.DB_TABLE)
				.append(" WHERE ").append(DbUser.KEYCLOAK_ID.id()).append(" = ?")
				.toString();
		
		try (Connection con = this.dataSource.getConnection();
				PreparedStatement pst = con.prepareStatement(sql)) {
			pst.setString(1, user.getId());
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					userInformation.put(DbUser.FIRST_NAME.id(), rs.getString(DbUser.FIRST_NAME.id()));
					userInformation.put(DbUser.LAST_NAME.id(), rs.getString(DbUser.LAST_NAME.id()));
					userInformation.put(DbUser.EMAIL.id(), rs.getString(DbUser.EMAIL.id()));
					userInformation.put(DbUser.PHONE.id(), rs.getString(DbUser.PHONE.id()));
					userInformation.put(DbUser.STREET.id(), rs.getString(DbUser.STREET.id()));
					userInformation.put(DbUser.CITY.id(), rs.getString(DbUser.CITY.id()));
					userInformation.put(DbUser.ZIP.id(), rs.getString(DbUser.ZIP.id()));
					userInformation.put(DbUser.COUNTRY.id(), rs.getString(DbUser.COUNTRY.id()));
				}
			}
		}
		return userInformation;
	}
	
	/**
	 * Writes updated user information from the userInformation JSON object to the DB. The keys in the JSON must match the DB column names, except for the address fields which are nested in the "address" object.
	 * @param user
	 * @param userInformation
	 * @throws SQLException
	 */
	public void setUserInformation(User user, JsonObject userInformation) throws SQLException {
	    StringBuilder setClause = new StringBuilder();
	    List<String> values = new ArrayList<>();

	    // Helper function to add to setClause and values list
	    Consumer<String> addToQuery = (key) -> {
	        if (userInformation.has(key) && !userInformation.get(key).getAsString().isEmpty()) {
	            if (setClause.length() > 0) {
	                setClause.append(", ");
	            }
	            setClause.append(key).append(" = ?");
	            values.add(userInformation.get(key).getAsString());
	        }
	    };
	    
	    /**
	     * Lambda function for nested address fields. Checks that nested address fields exist then adds them to the set clause and values list
	     */
	    Consumer<String> addToQueryAddress = (key) -> {
	    	if (userInformation.has("address") 
	    			&& userInformation.get("address").isJsonObject()
	    			&& userInformation.get("address").getAsJsonObject().has(key)
	    			&& !userInformation.get("address").getAsJsonObject().get(key).getAsString().isEmpty()) {
	    		if (setClause.length() > 0) {
	                setClause.append(", ");
	            }
	    		setClause.append(key).append(" = ?");
	    		values.add(userInformation.get("address").getAsJsonObject().get(key).getAsString());
	    	}
	    };

	    // Check and add each field if present
	    addToQuery.accept(DbUser.FIRST_NAME.id());
	    addToQuery.accept(DbUser.LAST_NAME.id());
	    addToQuery.accept(DbUser.EMAIL.id());
	    addToQuery.accept(DbUser.PHONE.id());
	    addToQueryAddress.accept(DbUser.STREET.id());
	    addToQueryAddress.accept(DbUser.CITY.id());
	    addToQueryAddress.accept(DbUser.ZIP.id());
	    addToQueryAddress.accept(DbUser.COUNTRY.id()); 

	    if (setClause.length() == 0) {
	        // No fields to update, return early or throw an exception
	        return;
	    }

	    StringBuilder sql = new StringBuilder()
	            .append("UPDATE ").append(DbUser.DB_TABLE)
	            .append(" SET ")
	            .append(setClause)
	            .append(" WHERE ").append(DbUser.KEYCLOAK_ID.id()).append(" = ?");
	    
	    values.add(user.getId());

	    try (var con = this.dataSource.getConnection();
	         var pst = con.prepareStatement(sql.toString())) {
	        // Set the values for the prepared statement
	        for (int i = 0; i < values.size(); i++) {
	            pst.setString(i + 1, values.get(i));
	        }
	        pst.execute();
	    }
	}
	
	/**
	 * Updates the preferred language for the user in the postgres DB
	 * @param user
	 * @param language
	 * @throws SQLException
	 */
	public void updateUserLanguage(User user, Language language) throws SQLException {
		String sql = new StringBuilder()
				.append("UPDATE ").append(DbUser.DB_TABLE)
				.append(" SET ")
				.append(DbUser.LANGUAGE.id()).append(" = ? ")
				.append("WHERE ").append(DbUser.KEYCLOAK_ID.id()).append(" = ?")
				.toString();
		
		try (var con = this.dataSource.getConnection();
				var pst = con.prepareStatement(sql)) {
			pst.setString(1, language.toString());
			pst.setString(2, user.getId());
			pst.execute();
		}
	}

	
	

}
