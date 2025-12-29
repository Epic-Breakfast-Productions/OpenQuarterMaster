package tech.ebp.oqm.plugin.storagotchi.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * This code is a band-aid to account for the OIDC flow re-adding the path prefix when behind a reverse proxy, doubling up on that prefix and causing 404's for the clients.
 * <p>
 * More details: https://stackoverflow.com/questions/79405434/running-quarkus-oidc-keycloak-behind-path-based-reverse-proxy
 */
@Slf4j
public class RedirectCheckPrefixFilter {

	@ServerResponseFilter
	public void checkPrefix(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
//		log.trace(
//			"Checking prefix. Is redirect: {}/{}  Had prefix header in request: {}",
//			responseContext.getStatusInfo().toEnum(),
//			Response.Status.FOUND.equals(responseContext.getStatusInfo().toEnum()),
//			requestContext.getHeaders().containsKey("x-forwarded-prefix")
//		);
		if (
			!Response.Status.FOUND.equals(responseContext.getStatusInfo().toEnum())
				|| !requestContext.getHeaders().containsKey("x-forwarded-prefix")
		) {
			log.trace("Response not needing prefix checking.");
			return;
		}

		String prefix = requestContext.getHeaderString("x-forwarded-prefix");
		URI redirectingToUri = URI.create(responseContext.getHeaderString("Location"));

		log.debug("Checking redirect response for doubled-up prefix: {} / {}", prefix, redirectingToUri);

		if (redirectingToUri.getPath().startsWith(prefix + prefix)) {
			log.debug("Detected doubled-up prefix.");
			URI newUri;
			try {
				newUri = new URI(
					redirectingToUri.getScheme(),
					redirectingToUri.getUserInfo(),
					redirectingToUri.getHost(),
					redirectingToUri.getPort(),
					redirectingToUri.getPath().replaceFirst(prefix, ""),
					redirectingToUri.getQuery(),
					redirectingToUri.getFragment()
				);
			} catch (URISyntaxException e) {
				log.error("FAILED to create new URL without doubled-up prefix", e);
				throw new RuntimeException(e);
			}
			responseContext.getHeaders().put("Location", List.of(newUri.toString()));
			log.info("Removed extra prefix from redirected URI. Initial: {} Resulting: {}", prefix, newUri);
		}
	}
}
