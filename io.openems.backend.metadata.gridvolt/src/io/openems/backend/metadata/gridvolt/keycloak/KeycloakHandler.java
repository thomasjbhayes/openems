package io.openems.backend.metadata.gridvolt.keycloak;


import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.openems.backend.common.metadata.User;
import io.openems.backend.metadata.gridvolt.Config;
import io.openems.backend.metadata.gridvolt.MetadataGridvolt;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.session.Role;


public class KeycloakHandler {
	
	private final MetadataGridvolt parent;
	private final String baseUrl;
	private String keycloakRealm;
	private String keycloakClientId;
	private String keycloakClientSecret;
	private PublicKey keycloakServerPublicKey;
	private Gson gson;
	
	private final Logger log = LoggerFactory.getLogger(KeycloakHandler.class);
	
	public KeycloakHandler(MetadataGridvolt parent, Config config) {
		this.parent = parent;
		this.baseUrl = String.format("%s:%s", config.keycloakUrl(), config.keycloakPort());
		this.keycloakRealm = config.keycloakRealm();
		this.keycloakClientId = config.keycloakClientId();
		this.keycloakClientSecret = config.keycloakClientSecret();
		this.keycloakServerPublicKey = null;
		
		this.gson = new GsonBuilder()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.create();	
	}
	
	
	/**
	 * Calls the Keycloak token endpoint. Uses the ROPC flow which must be configured on the server.
	 * @param username
	 * @param password
	 */
	public TokenResponse authenticate(String username, String password) {
				
		HttpClient client = HttpClient.newHttpClient();
		
		TokenResponse tokenResponse = null;
		
		Map<Object, Object> data = new HashMap<>();
		data.put("client_id", this.keycloakClientId);
		data.put("client_secret", this.keycloakClientSecret);
		data.put("username", username);
		data.put("password", password);
		data.put("grant_type", "password");
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(this.getTokenEndpointUrl()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(buildFormDataFromMap(data))
				.build();
		
		try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            tokenResponse = this.gson.fromJson(response.body(), TokenResponse.class);
            log.info("Response status code: " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		return tokenResponse;
	}
	
	/**
	 * Validates an access token and returns the user object if the token is valid
	 * @param token
	 * @return The user that corresponds to the token
	 */
	public User validateToken(String token) throws OpenemsNamedException {
		
		User user = null;
		
		if (token == null) {
			return user;
		}
		
		PublicKey publicKey = this.getKeycloakPublicKey();
		
		try {
			// If the JWT parses and a user exists for the token, we should probably create a user in the DB. Currently the DB user needs to be inserted manually
			Map<String, Object> userDetails = parseJwtToken(token, publicKey);
			String username = userDetails.get("preferred_username").toString();
			String userId = userDetails.get("sub").toString();
			user = this.parent.getPostgresHandler().user.getUser(userId, username, token);
			HashMap<String, Role> roles = this.parent.getPostgresHandler().edge.getEdgeRolesForUser(user);
			
			for (Map.Entry<String, Role> entry : roles.entrySet()) {
				user.setRole(entry.getKey(), entry.getValue());
			}
			return user;
		} catch (Exception e) {
            e.printStackTrace();
            throw new OpenemsException(e.getMessage());
        }
				
	}
	
	/***
	 * Logs out the current user. Calls the Keycloak logout endpoint.
	 * @param user
	 * @param refreshToken
	 */
	public void logout(User user, String refreshToken) {
		
		HttpClient client = HttpClient.newHttpClient();
		
		Map<Object, Object> data = new HashMap<>();
		data.put("client_id", this.keycloakClientId);
		data.put("client_secret", this.keycloakClientSecret);
		data.put("refresh_token", refreshToken);
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(this.getLogoutEndpointUrl()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(buildFormDataFromMap(data))
				.build();

		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			log.info("Response status code: " + response.statusCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Calls the token introspection endpoint and returns information about a specific access_token
	 * @param token
	 */
	public void introspectToken(String token) {
		
		HttpClient client = HttpClient.newHttpClient();
		
		Map<Object, Object> data = new HashMap<>();
		data.put("client_id", this.keycloakClientId);
		data.put("client_secret", this.keycloakClientSecret);
		data.put("token", token);
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(this.getTokenIntrospectionUrl()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(buildFormDataFromMap(data))
				.build();
		
		try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Response status code: " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
		
	/**
	 * Makes a request to the Keycloak certs endpoint to get the public key
	 * @return PublicKey or null
	 */
	public PublicKey fetchKeycloakPublicKeyFromServer() {
		
		PublicKey publicKey = null;
		HttpClient client = HttpClient.newHttpClient();
		
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(this.getCertsUrl()))
				.GET()
				.build();
		
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			log.info("Response status code: " + response.statusCode());
            
            if (response.statusCode() == 200) {
                publicKey = extractPublicKeyFromJwks(response.body());
            } else {
            	log.info("Failed to retrieve JWKS. HTTP Status Code: " + response.statusCode());
            }
            
		} catch (Exception e) {
            e.printStackTrace();
        }
		return publicKey;
	}
	
	/***
	 * Returns the publicKey for the Keycloak server. If we have already fetched the key, it uses the stored version
	 * If not, it fetches it from the server.
	 * @return The Public Key
	 */
	private PublicKey getKeycloakPublicKey() {
		if (this.keycloakServerPublicKey == null) {
			this.keycloakServerPublicKey = this.fetchKeycloakPublicKeyFromServer();
		}
		return this.keycloakServerPublicKey;
	}
	
	private String getTokenEndpointUrl() {
		return String.format("%s/auth/realms/%s/protocol/openid-connect/token", this.baseUrl, this.keycloakRealm); 
	}
	
	private String getLogoutEndpointUrl() {
		return String.format("%s/auth/realms/%s/protocol/openid-connect/logout", this.baseUrl, this.keycloakRealm);
	}
	
	private String getTokenIntrospectionUrl() {
		return String.format("%s/auth/realms/%s/protocol/openid-connect/token/introspect", this.baseUrl, this.keycloakRealm);
	}
	
	private String getCertsUrl() {
		return String.format("%s/auth/realms/%s/protocol/openid-connect/certs", this.baseUrl, this.keycloakRealm);
	}
	
	private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringJoiner("&");
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            builder.add(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8) + "="
                    + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
	
	/**
	 * Gets the public key for the server from the JWKS response we fetched from the server.
	 * @param jwksResponse
	 * @return
	 * @throws Exception
	 */
	private static PublicKey extractPublicKeyFromJwks(String jwksResponse) throws Exception {
	    JsonObject jwks = JsonParser.parseString(jwksResponse).getAsJsonObject();
	    String x5cEncoded = jwks.getAsJsonArray("keys").get(0).getAsJsonObject().get("x5c").getAsString();

	    // Decode the base64 encoded certificate
	    byte[] decoded = Base64.getDecoder().decode(x5cEncoded);

	    // Generate X.509 certificate from the decoded bytes
	    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
	    X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(decoded));

	    // Extract public key from the certificate
	    PublicKey publicKey = certificate.getPublicKey();
	    return publicKey;
	}

	
	/**
	 * Verifies and parses the JWT token with the public key and returns the claims.
	 * @param jwtToken
	 * @param publicKey
	 * @return
	 */
	private static Map<String, Object> parseJwtToken(String jwtToken, PublicKey publicKey) {
        Jws<Claims> jwsClaims = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(jwtToken);
 
        return jwsClaims.getPayload();
    }


}
