package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ServiceType;

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
}
