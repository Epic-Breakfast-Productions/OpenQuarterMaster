package tech.ebp.oqm.baseStation.model.rest.externalService;

//TODO:: for general, plugin

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.GeneralService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.ServiceType;
import tech.ebp.oqm.baseStation.model.rest.externalService.ExternalServiceSetupRequest;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GeneralServiceSetupRequest extends ExternalServiceSetupRequest {
	
	public ServiceType getServiceType() {
		return ServiceType.GENERAL;
	}
	
	@Override
	public GeneralService toExtService() {
		GeneralService newService = new GeneralService();
		this.setCoreData(newService);
		return newService;
	}
}
