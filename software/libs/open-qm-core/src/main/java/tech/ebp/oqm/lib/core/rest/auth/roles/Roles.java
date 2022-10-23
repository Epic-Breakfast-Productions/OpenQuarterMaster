package tech.ebp.oqm.lib.core.rest.auth.roles;

import java.util.Map;

public final class Roles {
	/*
		'type' roles
	 */
	/** Role for a human user. */
	public static final String USER = "user";
	public static final String USER_DESCRIPTION = "Role for a human user. Applied automatically to all user accounts.";
	/** Role for a service. */
	public static final String SERVICE = "service";
	public static final String SERVICE_DESCRIPTION = "Role for service. Applied automatically to all service accounts.";
	
	/*
		Admin Roles
	 */
	public static final String USER_ADMIN = "userAdmin";
	public static final String USER_ADMIN_DESCRIPTION = "Role to enable user administration. Can enable/disable users, and " +
														"control their roles.";
	
	public static final String SERVICE_ADMIN = "serviceAdmin";
	public static final String SERVICE_ADMIN_DESCRIPTION = "Role to enable service administration. Can enable/disable services, and " +
														   "control their roles.";
	
	public static final String INVENTORY_ADMIN = "inventoryAdmin";
	public static final String INVENTORY_ADMIN_DESCRIPTION = "Role to enable inventory administration. Can import/export inventory data.";
	
	/*
		inventory roles
	 */
	public static final String INVENTORY_VIEW = "inventoryView";
	public static final String INVENTORY_VIEW_DESCRIPTION = "Role to enable viewing inventory.";
	
	public static final String INVENTORY_EDIT = "inventoryEdit";
	public static final String INVENTORY_EDIT_DESCRIPTION = "Role to enable editing inventory.";
	
	
	public static final Map<String, String> ROLE_DESCRIPTION_MAP = Map.of(
		USER, USER_DESCRIPTION,
		SERVICE, SERVICE_DESCRIPTION,
		
		USER_ADMIN, USER_ADMIN_DESCRIPTION,
		SERVICE_ADMIN, SERVICE_ADMIN_DESCRIPTION,
		INVENTORY_ADMIN, INVENTORY_ADMIN_DESCRIPTION,
		
		INVENTORY_VIEW, INVENTORY_VIEW_DESCRIPTION,
		INVENTORY_EDIT, INVENTORY_EDIT_DESCRIPTION
	);
	
}
