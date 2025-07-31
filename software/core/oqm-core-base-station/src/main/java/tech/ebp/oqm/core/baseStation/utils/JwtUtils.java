package tech.ebp.oqm.core.baseStation.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.baseStation.model.UserInfo;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;

import java.util.Set;

@ApplicationScoped
public class JwtUtils {
	
	public static String getAuthId(JsonWebToken jwt){
		return jwt.getClaim(Claims.sub);
	}
	public static String getName(JsonWebToken jwt){
		return jwt.getClaim("name");
	}
	public static String getEmail(JsonWebToken jwt){
		return jwt.getClaim(Claims.email);
	}
	public static String getUserName(JsonWebToken jwt){
		return jwt.getClaim(Claims.preferred_username);
	}
	public static Set<String> getRoles(JsonWebToken jwt){
		return jwt.getGroups();
	}
	
	public static UserInfo getUserInfo(JsonWebToken jwt){
		return UserInfo.builder()
				   .authId(getAuthId(jwt))
				   .name(getName(jwt))
				   .username(getUserName(jwt))
				   .email(getEmail(jwt))
				   .roles(getRoles(jwt))
				   .build();
	}
}
