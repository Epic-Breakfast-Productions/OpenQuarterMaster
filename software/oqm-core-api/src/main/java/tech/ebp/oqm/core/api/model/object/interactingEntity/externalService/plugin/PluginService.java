package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.eclipse.microprofile.jwt.JsonWebToken;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.core.api.model.rest.externalService.ExternalServiceSetupRequest;
import tech.ebp.oqm.core.api.model.rest.externalService.PluginServiceSetupRequest;

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
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public InteractingEntityType getInteractingEntityType() {
		return InteractingEntityType.SERVICE_PLUGIN;
	}
	
	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		//TODO
		return false;
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
