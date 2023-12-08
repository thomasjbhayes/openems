package io.openems.backend.metadata.gridvolt.keycloak;

public class TokenResponse {
	public String accessToken;
	
	public Integer expiresIn;
	public Integer refreshExpiresIn;
	public String refreshToken;
	public String tokenType;
	public String notBeforePolicy;
	public String sessionState;
	public String scope;
}
