package tech.ebp.oqm.lib.core.api.quarkus.runtime.sso;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.rest.client.reactive.ClientFormParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.headerFactories.OidcClientAuthHeaderFactory;

@Path("/protocol/openid-connect/token")
@RegisterRestClient(configKey = Constants.CORE_API_CLIENT_OIDC_NAME)
@RegisterClientHeaders(OidcClientAuthHeaderFactory.class)
public interface KeycloakRestClient {
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ClientFormParam(name = "grant_type", value = "client_credentials")
	public ObjectNode getServiceAccountToken();
	
}
