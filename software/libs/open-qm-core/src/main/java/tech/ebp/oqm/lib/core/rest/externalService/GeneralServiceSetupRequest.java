package tech.ebp.oqm.lib.core.rest.externalService;

//TODO:: for general, plugin

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.object.externalService.GeneralService;
import tech.ebp.oqm.lib.core.object.externalService.ServiceType;

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
