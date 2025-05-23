package tech.ebp.oqm.core.api.model.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;

public interface HasParent {
	ObjectId getParent();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	default boolean hasParent() {
		return this.getParent() != null;
	}
}
