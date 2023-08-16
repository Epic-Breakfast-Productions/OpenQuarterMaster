package tech.ebp.oqm.baseStation.interfaces;

import io.quarkus.oidc.IdToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
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
	@Inject
	InteractingEntityService interactingEntityService;
	
	@Getter(AccessLevel.PROTECTED)
	@Context
	SecurityContext securityContext;
	
	@Getter(AccessLevel.PROTECTED)
	InteractingEntity interactingEntity = null;
	
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
	
	
	protected Optional<InteractingEntity> logRequestAndProcessEntity() {
		if (this.getUserToken() == null) {
			log.info("Processing request with no JWT; ssh:{}", this.getSecurityContext().isSecure());
			return Optional.empty();
		} else {
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
			
			this.interactingEntity = this.getInteractingEntityService().getEntity(this.getUserToken());
			
			return Optional.of(this.interactingEntity);
		}
	}
	
	protected InteractingEntity logRequestAndProcessEntityRequireEntity() {
		Optional<InteractingEntity> entityOptional = this.logRequestAndProcessEntity();
		
		if(entityOptional.isEmpty()){
			//TODO:: review this
			throw new ForbiddenException("Required to have auth. No entity.");
		}
		
		return entityOptional.get();
	}
	
	@PostConstruct
	void initialLogAndEntityProcess(){
		this.logRequestAndProcessEntity();
	}
}
