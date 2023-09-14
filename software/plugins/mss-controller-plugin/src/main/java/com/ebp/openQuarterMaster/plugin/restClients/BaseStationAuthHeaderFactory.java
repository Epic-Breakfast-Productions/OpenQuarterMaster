package com.ebp.openQuarterMaster.plugin.restClients;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Base64;

@ApplicationScoped
public class BaseStationAuthHeaderFactory implements ClientHeadersFactory {
	
	@RestClient
	KeycloakRestClient keycloakRestClient;
	
	@Override
	public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
		MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
		//TODO:: only do this every time if needed; cache and refresh
		result.add("Authorization", "Bearer " + this.keycloakRestClient.getServiceAccountToken().get("access_token").asText());
		return result;
	}

}
