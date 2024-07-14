package com.ebp.openQuarterMaster.plugin.restClients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vertx.ext.auth.impl.jose.JWT;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@ApplicationScoped
public class KcClientAuthService {
	@RestClient
	KeycloakRestClient keycloakRestClient;
	
	private String authString = null;
	private LocalDateTime lastRefresh = LocalDateTime.now();
	private int refreshAfter = 0;
	
	public synchronized String getAuthString(){
		if(
			this.authString == null
			|| this.lastRefresh.plus(this.refreshAfter, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())
		){
			log.info("Getting new service account token");
			ObjectNode result = this.keycloakRestClient.getServiceAccountToken();
			String jwt = result.get("access_token").asText();
			this.authString = "Bearer " + jwt;
			this.refreshAfter = (result.get("expires_in").asInt() / 3) * 2;
			this.lastRefresh = LocalDateTime.now();
			log.debug(
				"Got new auth token: {} / {}",
				jwt,
				JWT.parse(jwt)
			);
		}
		return authString;
	}
}
