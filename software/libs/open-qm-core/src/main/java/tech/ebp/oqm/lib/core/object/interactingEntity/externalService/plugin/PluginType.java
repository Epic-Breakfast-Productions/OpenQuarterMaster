package tech.ebp.oqm.lib.core.object.interactingEntity.externalService.plugin;

/**
 * Other ideas: - Overview page tab entry
 */
public enum PluginType {
	NAV_ITEM(PluginTypeType.PAGE_COMPONENT),
	NAV_SUB_MENU(PluginTypeType.PAGE_COMPONENT);
	
	public final PluginTypeType type;
	
	PluginType(PluginTypeType type) {
		this.type = type;
	}
}
