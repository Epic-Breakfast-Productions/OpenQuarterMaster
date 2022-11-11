package tech.ebp.oqm.lib.core.rest.externalService;

//TODO:: for general, plugin

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.object.externalService.ServiceType;
import tech.ebp.oqm.lib.core.object.externalService.plugin.Plugin;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PluginServiceSetupRequest extends ServiceSetupRequest {
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<Plugin> pageComponents = new ArrayList<>();
	
	public ServiceType getServiceType() {
		return ServiceType.PLUGIN;
	}
}
