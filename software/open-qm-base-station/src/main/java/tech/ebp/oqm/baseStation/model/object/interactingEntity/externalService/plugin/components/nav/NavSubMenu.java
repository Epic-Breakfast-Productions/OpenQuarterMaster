package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.components.nav;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.Plugin;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginType;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class NavSubMenu extends Plugin {
	
	@Override
	public PluginType getPluginType() {
		return PluginType.NAV_SUB_MENU;
	}
	
	/**
	 * The text of the menu
	 */
	@NonNull
	@NotNull
	private String menuText;
	
	/**
	 * The list of nav items to show in the menu
	 */
	@NotNull
	@NonNull
	@NotEmpty
	@lombok.Builder.Default
	private List<NavItem> menuItems = new ArrayList<>();
}
