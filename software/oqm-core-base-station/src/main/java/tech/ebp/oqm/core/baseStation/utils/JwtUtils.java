package tech.ebp.oqm.core.baseStation.utils;

import jakarta.json.Json;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.baseStation.model.UserInfo;

import java.util.Set;

public class JwtUtils {
	
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
				   .name(getName(jwt))
				   .roles(getRoles(jwt))
				   .build();
	}
}
