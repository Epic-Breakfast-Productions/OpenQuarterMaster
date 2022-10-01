package tech.ebp.oqm.lib.core.object.service.plugin.components;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.net.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class NavItemDetails extends PageComponentDetails {
	
	@Override
	public PageComponentType getComponentType() {
		return PageComponentType.NAV_ITEM;
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
