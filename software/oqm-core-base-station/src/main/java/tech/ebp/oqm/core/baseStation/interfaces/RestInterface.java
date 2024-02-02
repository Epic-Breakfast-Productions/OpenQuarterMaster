package tech.ebp.oqm.core.baseStation.interfaces;

import io.quarkus.oidc.IdToken;
import io.vertx.ext.auth.User;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.baseStation.model.UserInfo;
import tech.ebp.oqm.core.baseStation.utils.JwtUtils;

import java.util.Optional;

@Slf4j
@NoArgsConstructor
public abstract class RestInterface {
	
	@Getter(AccessLevel.PROTECTED)
	@Inject
	@IdToken
	JsonWebToken idToken;
	
	@Getter(AccessLevel.PROTECTED)
	@Inject
	JsonWebToken accessToken;
	
	@Getter(AccessLevel.PROTECTED)
	@Context
	SecurityContext securityContext;
	
	@Getter(AccessLevel.PROTECTED)
	UserInfo userInfo;
	
	protected boolean hasIdToken() {
		return this.getIdToken() != null && this.getIdToken().getClaimNames() != null;
	}
	
	protected boolean hasAccessToken(){
		return this.getAccessToken() != null && this.getAccessToken().getClaimNames() != null;
	}
	
	/**
	 * When hit from bare API call with just bearer, token will be access token.
	 *
	 * When hit from ui, idToken.
	 * @return
	 */
	protected JsonWebToken getUserToken(){
		if(this.hasIdToken()){
			return this.getIdToken();
		}
		if(this.hasAccessToken()){
			return this.getAccessToken();
		}
		return null;
	}
	
	private UserInfo logRequestAndProcessEntity() {
		log.info(
			"Processing request with JWT; User:{} ssh:{} jwtIssuer: {} roles: {}",
			this.getSecurityContext().getUserPrincipal().getName(),
			this.getSecurityContext().isSecure(),
			idToken.getIssuer(),
			idToken.getGroups()
		);
		if (this.getSecurityContext().isSecure()) {
			log.warn("Request with JWT made without HTTPS");
		}
		
		this.userInfo = JwtUtils.getUserInfo(this.getUserToken());
		
		return this.getUserInfo();
	}
	
	@PostConstruct
	void initialLogAndEntityProcess(){
		this.logRequestAndProcessEntity();
	}
}