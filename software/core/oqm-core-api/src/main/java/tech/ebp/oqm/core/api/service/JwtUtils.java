package tech.ebp.oqm.core.api.service;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

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
}
