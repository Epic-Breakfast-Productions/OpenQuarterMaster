package com.ebp.openQuarterMaster.plugin.restClients.headerFactories;


import com.ebp.openQuarterMaster.plugin.restClients.KeycloakRestClient;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Slf4j
@ApplicationScoped
public class BaseStationAuthHeaderFactory implements ClientHeadersFactory {
	
	@RestClient
	KeycloakRestClient keycloakRestClient;
	
	private String authString = null;
	private LocalDateTime lastRefresh = LocalDateTime.now();
	private int refreshAfter = 0;
	
	private synchronized String getAuthString(){
		if(
			this.authString == null
			|| this.lastRefresh.plus(this.refreshAfter, ChronoUnit.SECONDS).isBefore(LocalDateTime.now())
		){
			log.info("Getting new service account token");
			ObjectNode result = this.keycloakRestClient.getServiceAccountToken();
			this.authString = "Bearer " + result.get("access_token").asText();
			this.refreshAfter = (result.get("expires_in").asInt() / 3) * 2;
			this.lastRefresh = LocalDateTime.now();
		}
		return authString;
	}
	
	@Override
	public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
		MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
		result.add("Authorization", this.getAuthString());
		return result;
	}

}
