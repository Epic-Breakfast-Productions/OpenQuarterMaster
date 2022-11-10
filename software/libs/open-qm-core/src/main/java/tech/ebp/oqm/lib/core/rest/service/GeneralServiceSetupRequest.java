package tech.ebp.oqm.lib.core.rest.service;

//TODO:: for general, plugin

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.object.service.ServiceType;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GeneralServiceSetupRequest extends ServiceSetupRequest {
	
	public ServiceType getServiceType() {
		return ServiceType.GENERAL;
	}
}
