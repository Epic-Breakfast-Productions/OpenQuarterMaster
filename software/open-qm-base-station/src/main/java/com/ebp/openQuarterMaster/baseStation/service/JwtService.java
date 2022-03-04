package com.ebp.openQuarterMaster.baseStation.service;

import com.ebp.openQuarterMaster.lib.core.rest.user.UserLoginResponse;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.jwt.util.KeyUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;

import javax.enterprise.context.ApplicationScoped;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class JwtService {
	
	public static final String JWT_USER_ID_CLAIM = "userId";
	public static final String JWT_USER_TITLE_CLAIM = "title";
	public static final String JWT_ISSUER_CLAIM = Claims.iss.name();
	
	private final long defaultExpiration;
	private final long extendedExpiration;
	private final String sigKeyId;
	private final String issuer;
	private final PrivateKey privateKey;
	
	public JwtService(
		@ConfigProperty(name = "mp.jwt.verify.privatekey.location")
			String privateKeyLocation,
		@ConfigProperty(name = "mp.jwt.expiration.default")
			long defaultExpiration,
		@ConfigProperty(name = "mp.jwt.expiration.extended")
			long extendedExpiration,
		@ConfigProperty(name = "mp.jwt.verify.issuer")
			String issuer
	) throws Exception {
		this.defaultExpiration = defaultExpiration;
		this.extendedExpiration = extendedExpiration;
		this.sigKeyId = privateKeyLocation;
		this.issuer = issuer;
		
		log.info("Private key location: {}", privateKeyLocation);
		this.privateKey =
			KeyUtils.readPrivateKey(
				privateKeyLocation
			); //KeyUtils.readPrivateKey(privateKeyLocation); //     StaticUtils.resourceAsUrl(privateKeyLocation).toString());
	}
	
	/**
	 * Gets a user's jwt. Meant to be used during auth, returns the object meant to return to the user.
	 *
	 * @param user The user to get the jwt for
	 * @param extendedTimeout If the jwt should have an extended expiration period
	 *
	 * @return The response to give back to the user.
	 */
	public UserLoginResponse getUserJwt(User user, boolean extendedTimeout) {
		Instant expiration = Instant.now().plusSeconds((
														   extendedTimeout
															   ? this.extendedExpiration
															   : this.defaultExpiration
													   ));
		
		return new UserLoginResponse(this.generateTokenString(user, expiration), expiration);
	}
	
	/**
	 * Generates a jwt for use by the user.
	 *
	 * @param user The user to get the jwt for
	 * @param expires When the jwt should expire
	 *
	 * @return The jwt for the user
	 */
	public String generateTokenString(
		User user,
		Instant expires
	) {
		//info on what claims are: https://auth0.com/docs/security/tokens/json-web-tokens/json-web-token-claims
		Map<String, Object> rawClaims = this.getUserClaims(user);
		
		JwtClaimsBuilder claims = Jwt.claims(rawClaims);
		
		claims.expiresAt(expires);
		
		return claims.jws().keyId(this.sigKeyId).sign(this.privateKey);
	}
	
	private Map<String, Object> getUserClaims(User user) {
		Map<String, Object> output = this.getBaseClaims();
		
		String userIdentification = user.getId() + ";" + user.getEmail();
		
		output.put(
			"jti",
			//TODO:: this, properly
			//                user.getId() + "-" + user.getLastLogin().getTime() + "-" + user.getNumLogins()
			user.getId() + "-" + UUID.randomUUID()
		);//TODO: move to utility, test
		output.put(Claims.sub.name(), user.getId());
		output.put(Claims.aud.name(), userIdentification);
		output.put(Claims.upn.name(), user.getUsername());
		output.put(Claims.email.name(), user.getEmail());
		output.put(JWT_USER_TITLE_CLAIM, user.getTitle());
		output.put(Claims.given_name.name(), user.getFirstName());
		output.put(Claims.family_name.name(), user.getLastName());
		output.put(JWT_USER_ID_CLAIM, user.getId());
		
		output.put("roleMappings", new HashMap<String, Object>());
		
		output.put(Claims.groups.name(), user.getRoles());
		
		return output;
	}
	
	private Map<String, Object> getBaseClaims() {
		Map<String, Object> output = new HashMap<>();
		
		output.put(JWT_ISSUER_CLAIM, this.issuer); // serverInfo.getOrganization() + " - Task Timekeeper Server");
		output.put(Claims.auth_time.name(), Instant.now().getEpochSecond());
		
		return output;
	}
}
