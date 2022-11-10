package tech.ebp.oqm.lib.core.object.service.plugin.components.nav;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.object.service.plugin.components.PageComponent;
import tech.ebp.oqm.lib.core.object.service.plugin.components.PageComponentType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class NavSubMenu extends PageComponent {
	
	@Override
	public PageComponentType getComponentType() {
		return PageComponentType.NAV_SUB_MENU;
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
