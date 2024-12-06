package tech.ebp.oqm.plugin.alertMessenger.utils;

import java.util.UUID;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.plugin.alertMessenger.model.UserInfo;

import java.util.Set;

// Utility class for extracting user information (e.g., ID, email, roles) from JWT tokens.
public class JwtUtils {

	public static UUID getId(JsonWebToken jwt) {
		return UUID.fromString(jwt.getClaim(Claims.sub));
	}

	public static String getName(JsonWebToken jwt) {
		return jwt.getClaim("name");
	}

	public static String getEmail(JsonWebToken jwt) {
		return jwt.getClaim(Claims.email);
	}

	public static String getUserName(JsonWebToken jwt) {
		return jwt.getClaim(Claims.preferred_username);
	}

	public static Set<String> getRoles(JsonWebToken jwt) {
		return jwt.getGroups();
	}

	// Extracts and maps all relevant user information from the JWT token into a UserInfo object.
	public static UserInfo getUserInfo(JsonWebToken jwt) {
		return UserInfo.builder()
				.id(getId(jwt))
				.name(getName(jwt))
				.username(getUserName(jwt))
				.email(getEmail(jwt))
				.roles(getRoles(jwt))
				.build();
	}
}
