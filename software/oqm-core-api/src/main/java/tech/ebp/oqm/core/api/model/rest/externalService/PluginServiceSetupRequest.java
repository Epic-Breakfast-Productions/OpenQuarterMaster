package tech.ebp.oqm.core.api.model.rest.externalService;

//TODO:: for general, plugin

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.ServiceType;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.Plugin;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.PluginService;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PluginServiceSetupRequest extends ExternalServiceSetupRequest {
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<Plugin> pageComponents = new ArrayList<>();
	
	public ServiceType getServiceType() {
		return ServiceType.PLUGIN;
	}
	
	@Override
	public PluginService toExtService() {
		PluginService newService = new PluginService();
		this.setCoreData(newService);
		newService.setDisabledPageComponents(this.getPageComponents());
		return newService;
	}
}
