package tech.ebp.oqm.lib.core.api.java.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.api.java.config.KeycloakConfig;
import tech.ebp.oqm.lib.core.api.java.utils.UriUtils;
import tech.ebp.oqm.lib.core.api.java.utils.jackson.JacksonObjectNodeBodyHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Handles credentials for a service account (used with keycloak)
 */
@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
public class KCServiceAccountCreds extends OqmCredentials {
	private KeycloakConfig keycloakConfig;
	
	private String token = null;
	private LocalDateTime tokenExpires = null;
	private final ReentrantLock tokenMutex = new ReentrantLock();
	
	public KCServiceAccountCreds(@NonNull KeycloakConfig keycloakConfig) {
		this();
		this.keycloakConfig = keycloakConfig;
		if(this.keycloakConfig.getHttpClient() == null) {
			throw new IllegalArgumentException("Keycloak HTTP client must be provided.");
		}
	}
	
	public void refreshToken() {
		ObjectNode result = null;
		try {
			
			Map<String, String> parameters = new HashMap<>();
			parameters.put("grant_type", "client_credentials");
			String form = parameters.entrySet()
							  .stream()
							  .map(e ->e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
							  .collect(Collectors.joining("&"));
			
			HttpResponse<ObjectNode> response = this.keycloakConfig.getHttpClient()
				.send(
					HttpRequest.newBuilder()
						.uri(UriUtils.buildUri(this.keycloakConfig.getBaseUri(), "/realms/" + this.keycloakConfig.getRealm() + "/protocol/openid-connect/token"))
						.header("Authorization", "Basic: " + Base64.getEncoder().encodeToString((this.keycloakConfig.getClientId() + ":" + this.getKeycloakConfig().getClientSecret()).getBytes()))
						.header("Content-Type", "application/x-www-form-urlencoded")
						.POST(HttpRequest.BodyPublishers.ofString(form))
						.build(),
					JacksonObjectNodeBodyHandler.INSTANCE
				);
			
			//TODO:: validate? unclear if would throw exception on non 200
			
			result = response.body();
		} catch(IOException|InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		String jwt = result.get("access_token").asText();
		LocalDateTime expiresAt = LocalDateTime.now().plusSeconds((result.get("expires_in").asInt() / 4) * 3);
		
		this.tokenMutex.lock();
		try {
			this.token = jwt;
			this.tokenExpires = expiresAt;
		} finally {
			this.tokenMutex.unlock();
		}
	}
	
	private void refreshTokenIfNeeded() {
		if(this.token == null || this.tokenExpires.isBefore(LocalDateTime.now())){
			this.refreshToken();
		}
	}
	
	public String getToken() {
		this.tokenMutex.lock();
		try {
			this.refreshTokenIfNeeded();
			
			return this.token;
		} finally {
			this.tokenMutex.unlock();
		}
	}
	
	
	@Override
	public String getAccessHeaderContent() {
		return "Bearer " + this.getToken();
	}
}
