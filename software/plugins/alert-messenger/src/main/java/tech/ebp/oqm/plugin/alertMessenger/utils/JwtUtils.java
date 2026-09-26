package tech.ebp.oqm.plugin.alertMessenger.utils;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;

public class JwtUtils {

    private JwtUtils() {
        /* This utility class should not be instantiated */
    }

    public static String getId(JsonWebToken jwt) {
        return jwt.getClaim(Claims.sub).toString();
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
}
