package tech.ebp.oqm.lib.core.object.interactingEntity.externalService.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.ServiceType;
import tech.ebp.oqm.lib.core.rest.externalService.ExternalServiceSetupRequest;
import tech.ebp.oqm.lib.core.rest.externalService.PluginServiceSetupRequest;

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
	List<Plugin> enabledPageComponents = new ArrayList<>();
	
	@NonNull
	@NotNull
	List<Plugin> disabledPageComponents = new ArrayList<>();
	
	public List<Plugin> getAllPlugins() {
		List<Plugin> output = new ArrayList<>();
		output.addAll(this.getEnabledPageComponents());
		output.addAll(this.getDisabledPageComponents());
		
		return output;
	}
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.PLUGIN;
	}
	
	@Override
	public boolean changedGiven(ExternalServiceSetupRequest newServiceIn) {
		if (super.changedGiven(newServiceIn)) {
			return true;
		}
		PluginServiceSetupRequest pluginServiceSetupRequest = (PluginServiceSetupRequest) newServiceIn;
		
		List<Plugin> allPlugins = this.getAllPlugins();
		
		if (
			allPlugins.size() != pluginServiceSetupRequest.getPageComponents().size()
		) {
			return true;
		}
		for (Plugin cur : pluginServiceSetupRequest.getPageComponents()) {
			if (!allPlugins.contains(cur)) {
				return true;
			}
		}
		
		return false;
	}
}
