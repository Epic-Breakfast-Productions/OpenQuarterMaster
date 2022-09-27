package tech.ebp.oqm.lib.core.object.service;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GeneralService extends Service {
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.GENERAL;
	}
}
