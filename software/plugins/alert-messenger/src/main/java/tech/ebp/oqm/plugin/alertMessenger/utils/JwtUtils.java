package tech.ebp.oqm.plugin.alertMessenger.utils;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;

import java.util.Set;

public class JwtUtils {
	
	public static String getId(JsonWebToken jwt){
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
				   .id(getId(jwt))
				   .name(getName(jwt))
				   .username(getUserName(jwt))
				   .email(getEmail(jwt))
				   .roles(getRoles(jwt))
				   .build();
	}
}
