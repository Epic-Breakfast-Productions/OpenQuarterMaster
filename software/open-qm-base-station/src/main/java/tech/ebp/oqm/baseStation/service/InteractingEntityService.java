package tech.ebp.oqm.baseStation.service;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.service.mongo.ExternalServiceService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.rest.auth.roles.Roles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@Traced
@ApplicationScoped
public class InteractingEntityService {
	
	@Inject
	UserService userService;
	
	@Inject
	ExternalServiceService externalServiceService;
	
	
	public InteractingEntity getFromJwt(JsonWebToken jwt) {
		if(jwt.getGroups().contains(Roles.USER)){
			return this.userService.getFromJwt(jwt);
		} else if(jwt.getGroups().contains(Roles.EXT_SERVICE)){
			return this.externalServiceService.getFromJwt(jwt);
		}
		//TODO:: better exception
		throw new IllegalArgumentException("JWT given not a user or external service.");
	}
	
	public InteractingEntity getFromHistoryEvent(HistoryEvent e){
		switch (e.getEntityType()){
			case USER:
				return this.userService.get(e.getEntityId());
			case EXTERNAL_SERVICE:
				return this.externalServiceService.get(e.getEntityId());
		}
		
		throw new IllegalArgumentException("Bad entity type... how? " + e);
	}
}
