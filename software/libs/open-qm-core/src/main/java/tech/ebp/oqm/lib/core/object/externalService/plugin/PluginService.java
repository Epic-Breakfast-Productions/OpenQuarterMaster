package tech.ebp.oqm.lib.core.object.externalService.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.externalService.ExternalService;
import tech.ebp.oqm.lib.core.object.externalService.ServiceType;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PluginService extends ExternalService {
	
	@NonNull
	@NotNull
	List<Plugin> pageComponents = new ArrayList<>();
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.PLUGIN;
	}
}
