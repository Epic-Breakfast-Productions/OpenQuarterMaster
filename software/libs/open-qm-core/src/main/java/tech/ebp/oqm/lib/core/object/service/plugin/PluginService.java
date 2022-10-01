package tech.ebp.oqm.lib.core.object.service.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.service.Service;
import tech.ebp.oqm.lib.core.object.service.ServiceType;
import tech.ebp.oqm.lib.core.object.service.plugin.components.PageComponentDetails;
import tech.ebp.oqm.lib.core.object.service.plugin.components.PageComponentType;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PluginService extends Service {
	
	@NonNull
	@NotNull
	Map<PageComponentType, PageComponentDetails> pageComponents = new HashMap<>();
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.PLUGIN;
	}
}
