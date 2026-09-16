package tech.ebp.oqm.lib.core.api.java.config;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import tech.ebp.oqm.lib.core.api.java.auth.KCServiceAccountCreds;

import java.net.URI;
import java.net.http.HttpClient;

/**
 * Configuration for connecting to a Keycloak instance
 */
@Data
@Builder(toBuilder = true)
@Setter(AccessLevel.PRIVATE)
public class KeycloakConfig {
	
	/**
	 * The base URI of the Keycloak instance to connect to.
	 * <p>
	 * Example: "http://localhost:8080"
	 */
	@NonNull
	private URI baseUri;
	
	/**
	 * The realm we are connecting to.
	 */
	@NonNull
	@Builder.Default
	private String realm = "oqm";
	
	/**
	 * The client id of the service account we are using.
	 */
	@NonNull
	private String clientId;
	
	/**
	 * The secret of the client we are using.
	 */
	@NonNull
	private String clientSecret;
	
	/**
	 * If a {@link KCServiceAccountCreds} credential should be built automatically as default for the overall client.
	 */
	@Builder.Default
	private boolean defaultCreds = false;
	
	/**
	 * The http client to use when communicating with Keycloak. Null to default to the same one used by the client.
	 */
	@Builder.Default
	private HttpClient httpClient = null;
}
