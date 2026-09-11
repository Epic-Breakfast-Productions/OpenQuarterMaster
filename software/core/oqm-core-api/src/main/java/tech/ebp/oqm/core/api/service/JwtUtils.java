package tech.ebp.oqm.core.api.service;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;

public class JwtUtils {
	public static final String CLAIM_NAME = "name";
	public static final String CLAIM_DEV_EMAIL = "devEmail";
	public static final String CLAIM_DEV_NAME = "devName";
	public static final String CLAIM_DEV_WEBSITE = "devWebsite";


	public static String getName(JsonWebToken jwt){
		return jwt.getClaim(CLAIM_NAME);
	}
	public static String getServiceName(JsonWebToken jwt){
		return jwt.getClaim(Claims.azp);
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
	public static String getDevEmail(JsonWebToken jwt){
		return jwt.getClaim(CLAIM_DEV_EMAIL);
	}
	public static String getDevName(JsonWebToken jwt){
		return jwt.getClaim(CLAIM_DEV_NAME);
	}
	public static String getDevWebsite(JsonWebToken jwt){
		return jwt.getClaim(CLAIM_DEV_WEBSITE);
	}
}
