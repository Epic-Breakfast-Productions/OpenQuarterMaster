package com.ebp.openQuarterMaster.baseStation.service;

import com.ebp.openQuarterMaster.baseStation.data.pojos.User;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserLoginResponse;
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
    public static final String JWT_USER_EMAIL_CLAIM = "userEmail";
    public static final String JWT_USER_TITLE_CLAIM = "userTitle";
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

        this.privateKey = KeyUtils.readPrivateKey(privateKeyLocation); //KeyUtils.readPrivateKey(privateKeyLocation); //     StaticUtils.resourceAsUrl(privateKeyLocation).toString());
    }


    public UserLoginResponse getUserJwt(User user, boolean extendedTimeout) {
        Instant expiration = Instant.now().plusSeconds((extendedTimeout
                ? this.extendedExpiration
                : this.defaultExpiration));


        return new UserLoginResponse(this.generateTokenString(user, expiration), expiration);
    }

    public String generateTokenString(
            User user,
            Instant expires
    ) {
        //info on what claims are: https://auth0.com/docs/security/tokens/json-web-tokens/json-web-token-claims
        JwtClaimsBuilder claims = Jwt.claims(this.getUserClaims(user));


        claims.expiresAt(expires);
        claims.claim(Claims.auth_time.name(), expires.getEpochSecond());

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
        output.put(Claims.sub.name(), userIdentification);
        output.put(Claims.aud.name(), userIdentification);
        output.put(Claims.upn.name(), user.getUsername());
        output.put(JWT_USER_EMAIL_CLAIM, user.getEmail());
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

        return output;
    }
}
