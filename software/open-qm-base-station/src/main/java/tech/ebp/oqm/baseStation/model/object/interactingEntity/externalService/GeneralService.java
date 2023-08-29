package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GeneralService extends ExternalService {
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public InteractingEntityType getInteractingEntityType() {
		return InteractingEntityType.SERVICE_GENERAL;
	}
	
	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		//TODO
		return false;
	}
}
