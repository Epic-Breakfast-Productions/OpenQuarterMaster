package tech.ebp.oqm.lib.core.api.java.config;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;

import java.net.URI;

@Data
@Builder(toBuilder = true)
@Setter(AccessLevel.PRIVATE)
public class CoreApiConfig {
	
	/**
	 * The base URI of the core api to connect to.
	 */
	@NonNull
	private URI baseUri;
	
	/**
	 * Optional, if connecting to Keycloak. The configuration to communicate with Keycloak.
	 */
	@Builder.Default
	private KeycloakConfig keycloakConfig = null;
}
