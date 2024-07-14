package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.components.nav;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.Plugin;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.plugin.PluginType;

import java.net.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class NavItem extends Plugin {
	
	@Override
	public PluginType getPluginType() {
		return PluginType.NAV_ITEM;
	}
	
	/**
	 * The text of the link
	 */
	@NonNull
	@NotNull
	private String itemText;
	
	/**
	 * The url of the nav item to go to
	 */
	@NonNull
	@NotNull
	private URL itemUrl;
	
	/**
	 * If the link should go to a new tab
	 */
	@lombok.Builder.Default
	private boolean newTab = false;
}
