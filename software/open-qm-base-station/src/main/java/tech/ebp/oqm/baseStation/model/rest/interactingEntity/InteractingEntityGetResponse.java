package tech.ebp.oqm.baseStation.model.rest.interactingEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractingEntityGetResponse {
	public static InteractingEntityGetResponse fromInteractingEntity(InteractingEntity entity){
		return new InteractingEntityGetResponse(
			entity.getId(),
			entity.getIdFromAuthProvider(),
			entity.getAuthProvider(),
			entity.getName(),
			entity.getEmail(),
			entity.getRoles()
		);
	}
	
	private ObjectId id;
	
	private String idFromAuthProvider;
	private String authProvider;
	private String name;
	private String email;
	private Set<String> roles;
}
