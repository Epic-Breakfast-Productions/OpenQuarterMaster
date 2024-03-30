package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin;

/**
 * Other ideas: - Overview page tab entry
 */
public enum PluginType {
	NAV_ITEM(tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginTypeType.PAGE_COMPONENT),
	NAV_SUB_MENU(tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginTypeType.PAGE_COMPONENT);
	
	public final tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.plugin.PluginTypeType type;
	
	PluginType(PluginTypeType type) {
		this.type = type;
	}
}
