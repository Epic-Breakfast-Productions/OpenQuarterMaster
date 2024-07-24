package tech.ebp.oqm.plugin.mssController.devTools.runtime.sso.headerFactories;


import io.netty.handler.codec.HeadersUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class OidcClientAuthHeaderFactory implements ClientHeadersFactory {

	boolean enabled = true;
	String authStr;
	
	@Inject
	public OidcClientAuthHeaderFactory(
		@ConfigProperty(name = "quarkus.oidc.client-id")
		Optional<String> clientId,
		@ConfigProperty(name = "quarkus.oidc.credentials.secret")
		Optional<String> clientSecret
	){
		if(clientId.isEmpty() || clientSecret.isEmpty()){
			log.info("No oidc client id or secret provided.");
			this.enabled = false;
			return;
		}

		log.debug("Got OIDC creds for keycloak. client id: {} secret: {}", clientId, clientSecret);
		this.authStr = "Basic: " + Base64.getEncoder().encodeToString((clientId.get()+":"+clientSecret.get()).getBytes());
		log.debug("Auth string for keycloak: {}", this.authStr);
	}
	
	@Override
	public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
		if(!this.enabled){
			throw new IllegalStateException("Not enabled to connect to Keycloak.");
		}
		log.debug("Adding auth header to call.");
		MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
		result.add("Authorization",this.authStr);
		return result;
	}

}
