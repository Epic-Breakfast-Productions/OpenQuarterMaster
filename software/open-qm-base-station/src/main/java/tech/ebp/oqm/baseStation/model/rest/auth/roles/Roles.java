package tech.ebp.oqm.baseStation.model.rest.auth.roles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Roles that define what entities are allowed to do in OpenQuarterMaster.
 */
public final class Roles {
	/*
		'type' roles
	 */
	/** Role for a human user. */
	public static final String USER = "user";
	public static final String USER_DESCRIPTION = "Role for a human user. Applied automatically to all user accounts.";
	/** Role for a service. */
	public static final String EXT_SERVICE = "extService";
	public static final String EXT_SERVICE_DESCRIPTION = "Role for service. Applied automatically to all service accounts.";
	
	/*
		Admin Roles
	 */
	
	//This will probably change to "pluginAdmin"
//	public static final String EXT_SERVICE_ADMIN = "extServiceAdmin";
//	public static final String EXT_SERVICE_ADMIN_DESCRIPTION = "Role to enable service administration. Can enable/disable services, and " +
//															   "control their roles. For plugins, can enable/disable individual plugin " +
//															   "entries.";
	
	public static final String INVENTORY_ADMIN = "inventoryAdmin";
	public static final String INVENTORY_ADMIN_DESCRIPTION = "Role to enable inventory administration. Can import/export inventory data.";
	
	/*
		inventory roles
	 */
	public static final String INVENTORY_VIEW = "inventoryView";
	public static final String INVENTORY_VIEW_DESCRIPTION = "Role to enable viewing inventory.";
	
	public static final String INVENTORY_EDIT = "inventoryEdit";
	public static final String INVENTORY_EDIT_DESCRIPTION = "Role to enable editing inventory.";
	
	public static final String INVENTORY_CHECKOUT = "itemCheckout";
	public static final String INVENTORY_CHECKOUT_DESCRIPTION = "Role to enable checking out (and back in) items.";
	
	/**
	 * Map to easily associate roles with their description.
	 */
	public static final Map<String, String> ROLE_DESCRIPTION_MAP = Collections.unmodifiableMap(new LinkedHashMap<>() {{
		this.put(USER, USER_DESCRIPTION);
		this.put(EXT_SERVICE, EXT_SERVICE_DESCRIPTION);
		
//		this.put(EXT_SERVICE_ADMIN, EXT_SERVICE_ADMIN_DESCRIPTION);
		this.put(INVENTORY_ADMIN, INVENTORY_ADMIN_DESCRIPTION);
		
		this.put(INVENTORY_VIEW, INVENTORY_VIEW_DESCRIPTION);
		this.put(INVENTORY_EDIT, INVENTORY_EDIT_DESCRIPTION);
		this.put(INVENTORY_CHECKOUT, INVENTORY_CHECKOUT_DESCRIPTION);
	}});
	
	public static final Set<String> ALL_ROLES = Collections.unmodifiableSet(ROLE_DESCRIPTION_MAP.keySet());
	
	public static final Set<String> ADMIN_ROLES = Set.of(INVENTORY_ADMIN);
	public static final Set<String> NON_ADMIN_ROLES = Collections.unmodifiableSet(
		ALL_ROLES.stream()
			.filter((String role )->!ADMIN_ROLES.contains(role))
			.collect(Collectors.toSet())
	);
}
