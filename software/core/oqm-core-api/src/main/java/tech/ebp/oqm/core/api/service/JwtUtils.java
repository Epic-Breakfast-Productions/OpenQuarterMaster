package tech.ebp.oqm.core.api.service;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class JwtUtils {

	public static String getName(JsonWebToken jwt) {
		Object claim = jwt.getClaim("name");
		return claim != null ? claim.toString() : null;
	}

	public static String getEmail(JsonWebToken jwt) {
		Object claim = jwt.getClaim(Claims.email);
		return claim != null ? claim.toString() : null;
	}

	public static String getUserName(JsonWebToken jwt) {
		Object claim = jwt.getClaim(Claims.preferred_username);
		return claim != null ? claim.toString() : null;
	}

	public static String getUpn(JsonWebToken jwt) {
		Object claim = jwt.getClaim(Claims.upn);
		return claim != null ? claim.toString() : null;
	}

	public static Set<String> getRoles(JsonWebToken jwt) {
		Set<String> groups = jwt.getGroups();
		return groups != null ? groups : Collections.emptySet();
	}

	public static boolean safeEquals(Object a, Object b) {
		return Objects.equals(a, b);
	}
}
