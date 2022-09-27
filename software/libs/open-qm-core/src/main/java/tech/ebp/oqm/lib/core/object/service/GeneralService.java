package tech.ebp.oqm.lib.core.object.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GeneralService extends Service {
	
	@NonNull
	@NotNull
	Map<String, URL> pageComponents = new HashMap<>();
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.GENERAL;
	}
}
