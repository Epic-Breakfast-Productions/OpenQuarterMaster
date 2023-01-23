package tech.ebp.oqm.baseStation.config;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class BaseStationInteractingEntity implements InteractingEntity {
	
	@Inject
	@ConfigProperty(name = "service.runBy.email", defaultValue = "")
	String devEmail;
	
	@Override
	public ObjectId getId() {
		return null;//TODO:: return service id?
	}
	
	@Override
	public String getName() {
		return "Base Station";
	}
	
	@Override
	public String getEmail() {
		return this.devEmail;
	}
	
	@Override
	public InteractingEntityType getInteractingEntityType() {
		return InteractingEntityType.BASE_STATION;
	}
}
