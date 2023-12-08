package io.openems.backend.metadata.gridvolt.keycloak;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
/*
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.AdapterTokenVerifier;
//import org.keycloak.admin.client.
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.adapters.config.AdapterConfig;
 */ 
import io.openems.backend.common.metadata.User;
import io.openems.backend.metadata.gridvolt.Config;
import io.openems.backend.metadata.gridvolt.MetadataGridvolt;
import io.openems.common.session.Language;
import io.openems.common.session.Role;


public class KeycloakHandler {
	
	private final MetadataGridvolt parent;
	private final String baseUrl;
	private String keycloakRealm;
	private String keycloakClientId;
	private String keycloakClientSecret;
	private PublicKey keycloakServerPublicKey;
	
	private final Logger log = LoggerFactory.getLogger(KeycloakHandler.class);
	
	public KeycloakHandler(MetadataGridvolt parent, Config config) {
		this.parent = parent;
		//this.config = config;
		this.baseUrl = String.format("%s:%s", config.keycloakUrl(), config.keycloakPort());
		this.keycloakRealm = config.keycloakRealm();
		this.keycloakClientId = config.keycloakClientId();
		this.keycloakClientSecret = config.keycloakClientSecret();
		this.keycloakServerPublicKey = null;
	}
	
	
	/**
	 * 
	 * @param username
	 * @param password
	 */
	public TokenResponse authenticate(String username, String password) {
		
		String tokenEndpointUrl = this.getTokenEndpointUrl();
		
		HttpClient client = HttpClient.newHttpClient();
		Gson gson = new Gson();
		
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
            tokenResponse = gson.fromJson(response.body(), TokenResponse.class);
            log.info("Response status code: " + response.statusCode());
            log.info("Response body: " + response.body());
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
	public User validateToken(String token) {
		
		User user = null;
		
		PublicKey publicKey = this.getKeycloakPublicKey();
		
		try {
			Map<String, Object> userDetails = parseJwtToken(token, publicKey);
			log.info("User Details: " + userDetails);
		} catch (Exception e) {
            e.printStackTrace();
        }
		
		return user; 
		
	}
	
	/**
	 * Calls the token introspection endpoint and returns information about a specific access_token
	 * @param token
	 */
	public void introspectToken(String token) {
		
		HttpClient client = HttpClient.newHttpClient();
		Gson gson = new Gson();
		
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
            log.info("Response body: " + response.body());
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
			log.info("Response body: " + response.body());
            
            if (response.statusCode() == 200) {
                publicKey = extractPublicKeyFromJwks(response.body());
                log.info("Public Key: " + publicKey);
            } else {
            	log.info("Failed to retrieve JWKS. HTTP Status Code: " + response.statusCode());
            }
            
		} catch (Exception e) {
            e.printStackTrace();
        }
		return publicKey;
	}
	
	private PublicKey getKeycloakPublicKey() {
		if (this.keycloakServerPublicKey == null) {
			this.keycloakServerPublicKey = this.fetchKeycloakPublicKeyFromServer();
		}
		return this.keycloakServerPublicKey;
	}
	
	private String getTokenEndpointUrl() {
		return String.format("%s/realms/%s/protocol/openid-connect/token", this.baseUrl, this.keycloakRealm); 
	}
	
	private String getTokenIntrospectionUrl() {
		return String.format("%s/realms/%s/protocol/openid-connect/token/introspect", this.baseUrl, this.keycloakRealm);
	}
	
	private String getCertsUrl() {
		return String.format("%s/realms/%s/protocol/openid-connect/certs", this.baseUrl, this.keycloakRealm);
	}
	
	private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
        var builder = new StringJoiner("&");
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            builder.add(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8) + "="
                    + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }
	
	private static PublicKey extractPublicKeyFromJwks(String jwksResponse) throws Exception {
        JsonObject jwks = JsonParser.parseString(jwksResponse).getAsJsonObject();
        String publicKeyEncoded = jwks.getAsJsonArray("keys").get(0).getAsJsonObject().get("x5c").getAsString();
        
        byte[] decoded = Base64.getDecoder().decode(publicKeyEncoded);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(spec);
    }
	
	private static Map<String, Object> parseJwtToken(String jwtToken, PublicKey publicKey) {
        Jws<Claims> jwsClaims = Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(jwtToken);
 
        return jwsClaims.getPayload();
    }


}
