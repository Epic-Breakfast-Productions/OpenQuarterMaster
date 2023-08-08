package tech.ebp.oqm.baseStation.interfaces;

import io.quarkus.oidc.IdToken;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.service.mongo.InteractingEntityService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public abstract class RestInterface {
	
	@Getter(AccessLevel.PROTECTED)
	@Inject
	@IdToken
	JsonWebToken jwt;
	
	@Getter(AccessLevel.PROTECTED)
	@Inject
	InteractingEntityService interactingEntityService;
	
	@Getter(AccessLevel.PROTECTED)
	@Context
	SecurityContext securityContext;
	
	@Getter(AccessLevel.PROTECTED)
	InteractingEntity interactingEntity = null;
	
	protected RestInterface(JsonWebToken jwt, InteractingEntityService interactingEntityService, SecurityContext securityContext) {
		this.jwt = jwt;
		this.interactingEntityService = interactingEntityService;
		this.securityContext = securityContext;
	}
	
	protected boolean hasJwt() {
		return this.getJwt() != null && this.getJwt().getClaimNames() != null;
	}
	
	protected Optional<InteractingEntity> logRequestAndProcessEntity() {
		if (!this.hasJwt()) {
			log.info("Processing request with no JWT; ssh:{}", this.getSecurityContext().isSecure());
			return Optional.empty();
		} else {
			log.info(
				"Processing request with JWT; User:{} ssh:{} jwtIssuer: {}",
				this.getSecurityContext().getUserPrincipal().getName(),
				this.getSecurityContext().isSecure(),
				jwt.getIssuer()
			);
			if (this.getSecurityContext().isSecure()) {
				log.warn("Request with JWT made without HTTPS");
			}
			
			this.interactingEntity = this.getInteractingEntityService().getEntity(this.getJwt());
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
