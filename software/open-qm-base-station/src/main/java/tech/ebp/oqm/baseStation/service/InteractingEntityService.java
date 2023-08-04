package tech.ebp.oqm.baseStation.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;
import tech.ebp.oqm.baseStation.service.mongo.ExternalServiceService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named("InteractingEntityService")
@ApplicationScoped
public class InteractingEntityService {
	
	@Inject
	UserService userService;
	
	@Inject
	ExternalServiceService externalServiceService;
	
	
	@WithSpan
	public InteractingEntity getEntity(JsonWebToken jwt) {
		if(jwt.getGroups().contains(Roles.USER)){
			return this.userService.getFromJwt(jwt);
		} else if(jwt.getGroups().contains(Roles.EXT_SERVICE)){
			return this.externalServiceService.getFromJwt(jwt);
		}
		//TODO:: better exception
		throw new IllegalArgumentException("JWT given not a user or external service.");
	}
	
	
	@WithSpan
	public InteractingEntity getEntity(InteractingEntityType entityType, ObjectId id){
		switch (entityType){
			case USER:
				return this.userService.get(id);
			case EXTERNAL_SERVICE:
				return this.externalServiceService.get(id);
		}
		
		throw new IllegalArgumentException("Bad entity type... how? " + entityType);
		
	}
	
	@WithSpan
	public InteractingEntity getEntity(InteractingEntityReference ref){
		return this.getEntity(ref.getEntityType(), ref.getEntityId());
	}
	
	@WithSpan
	public InteractingEntity getEntity(ObjectHistoryEvent e){
		return this.getEntity(e.getEntity());
	}
}
