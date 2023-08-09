package tech.ebp.oqm.baseStation.config;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class BaseStationInteractingEntity extends InteractingEntity {
	
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
	
	@Override
	public Set<String> getRoles() {
		return new HashSet<>(){{add("Yes");}};
	}
}
