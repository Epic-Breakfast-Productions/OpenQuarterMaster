package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ServiceType;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GeneralService extends ExternalService {
	
	@Override
	public tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ServiceType getServiceType() {
		return ServiceType.GENERAL;
	}
}
