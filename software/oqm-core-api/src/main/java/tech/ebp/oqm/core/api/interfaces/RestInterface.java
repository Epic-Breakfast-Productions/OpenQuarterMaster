package tech.ebp.oqm.core.api.interfaces;

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
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.service.mongo.InteractingEntityService;

import java.util.Optional;

@Slf4j
@NoArgsConstructor
public abstract class RestInterface {
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
		if(this.securityContext.getAuthenticationScheme().equals("Basic")){
			return null;
		}
		if(this.hasAccessToken()){
			return this.getAccessToken();
		}
		return null;
	}
	
	private Optional<InteractingEntity> logRequestAndProcessEntity() {
		if (this.securityContext.getAuthenticationScheme() == null) {
			log.info("Processing request with no authentication; ssh:{}", this.getSecurityContext().isSecure());
			return Optional.empty();
		} else {
			if (this.getSecurityContext().isSecure()) {
				log.warn("Request with Auth made without HTTPS");
			}
			
			this.interactingEntity = this.getInteractingEntityService().getEntity(this.getSecurityContext(), this.getUserToken());
			
			log.info("Processing request with Auth; interacting entity: {}", interactingEntity);
			return Optional.of(this.interactingEntity);
		}
	}
	
	protected InteractingEntity requireAndGetEntity() {
		InteractingEntity entity = this.getInteractingEntity();
		
		if(entity == null){
			//TODO:: review this
			throw new ForbiddenException("Required to have auth. No entity.");
		}
		
		return entity;
	}
	
	@PostConstruct
	void initialLogAndEntityProcess(){
		this.logRequestAndProcessEntity();
	}
}
