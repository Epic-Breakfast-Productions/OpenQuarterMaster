package tech.ebp.oqm.baseStation.model.object.interactingEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;

import java.util.Set;

public interface InteractingEntity {
	
	public ObjectId getId();
	
	public String getName();
	
	public String getEmail();
	
	public InteractingEntityType getInteractingEntityType();
	
	public Set<String> getRoles();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public default InteractingEntityReference getReference() {
		return new InteractingEntityReference(this.getId(), this.getInteractingEntityType());
	}
}
