package tech.ebp.oqm.baseStation.interfaces.endpoints;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.interfaces.RestInterface;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;

import jakarta.ws.rs.core.SecurityContext;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EndpointProvider extends RestInterface {
	
	private static final String ROOT_API_ENDPOINT = "/api";
	public static final String ROOT_API_ENDPOINT_V1 = ROOT_API_ENDPOINT + "/v1";
	
}
