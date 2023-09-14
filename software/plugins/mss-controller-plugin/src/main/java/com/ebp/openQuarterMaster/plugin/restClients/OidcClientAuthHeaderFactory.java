package com.ebp.openQuarterMaster.plugin.restClients;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.Base64;

@ApplicationScoped
public class OidcClientAuthHeaderFactory implements ClientHeadersFactory {
	
	String authStr;
	
	@Inject
	public OidcClientAuthHeaderFactory(
		@ConfigProperty(name = "quarkus.oidc.client-id")
		String clientId,
		@ConfigProperty(name = "quarkus.oidc.credentials.secret")
		String clientSecret
	){
		this.authStr = "Basic: " + Base64.getEncoder().encodeToString((clientId+":"+clientSecret).getBytes());
	}
	
	
	@Override
	public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
		MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
		result.add("Authorization",this.authStr);
		return result;
	}

}
