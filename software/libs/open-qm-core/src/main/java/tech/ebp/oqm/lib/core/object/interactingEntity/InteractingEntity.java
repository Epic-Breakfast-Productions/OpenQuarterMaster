package tech.ebp.oqm.lib.core.object.interactingEntity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityType;

public interface InteractingEntity {
	
	public ObjectId getId();
	
	public String getName();
	
	public String getEmail();
	
	public InteractingEntityType getInteractingEntityType();
}
