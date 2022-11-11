package tech.ebp.oqm.lib.core.object.externalService;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GeneralService extends ExternalService {
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.GENERAL;
	}
}
