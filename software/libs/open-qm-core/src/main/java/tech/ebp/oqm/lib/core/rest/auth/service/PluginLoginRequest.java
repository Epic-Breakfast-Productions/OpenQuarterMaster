package tech.ebp.oqm.lib.core.rest.auth.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.externalService.plugin.Plugin;
import tech.ebp.oqm.lib.core.object.externalService.plugin.PluginType;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PluginLoginRequest extends ServiceLoginRequest {
	
	@NonNull
	@NotNull
	private Map<PluginType, Plugin> pageComponents = new HashMap<>();
}
