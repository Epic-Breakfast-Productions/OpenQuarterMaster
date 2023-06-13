package tech.ebp.oqm.baseStation.interfaces.endpoints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.utils.AuthMode;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.SecurityContext;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EndpointProvider {
	
	private static final String ROOT_API_ENDPOINT = "/api";
	public static final String ROOT_API_ENDPOINT_V1 = ROOT_API_ENDPOINT + "/v1";
	
	
	protected static void assertSelfAuthMode(AuthMode authMode) {
		if (!AuthMode.SELF.equals(authMode)) {
			//TODO:: throw custom exception, handle to return proper response object
			throw new ForbiddenException("Service not set to authenticate its own users.");
		}
	}
	protected static void assertExternalAuthMode(AuthMode authMode) {
		if (!AuthMode.EXTERNAL.equals(authMode)) {
			//TODO:: throw custom exception, handle to return proper response object
			throw new ForbiddenException("Service not set to authenticate externally.");
		}
	}
	
	protected static void logRequestContext(JsonWebToken jwt, SecurityContext context) {
		if (!hasJwt(jwt)) {
			log.info("Processing request with no JWT; ssh:{}", context.isSecure());
		} else {
			log.info(
				"Processing request with JWT; User:{} ssh:{} jwtIssuer: {}",
				context.getUserPrincipal().getName(),
				context.isSecure(),
				jwt.getIssuer()
			);
			if (context.isSecure()) {
				log.warn("Request with JWT made without HTTPS");
			}
		}
		
	}
	
	protected static boolean hasJwt(JsonWebToken jwt) {
		return jwt != null && jwt.getClaimNames() != null;
	}
}
