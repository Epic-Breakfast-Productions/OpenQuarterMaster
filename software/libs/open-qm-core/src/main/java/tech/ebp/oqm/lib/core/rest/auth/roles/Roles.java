package tech.ebp.oqm.lib.core.rest.auth.roles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
	public static final String USER_ADMIN = "userAdmin";
	public static final String USER_ADMIN_DESCRIPTION = "Role to enable user administration. Can enable/disable users, and " +
														"control their roles.";
	
	public static final String EXT_SERVICE_ADMIN = "extServiceAdmin";
	public static final String EXT_SERVICE_ADMIN_DESCRIPTION = "Role to enable service administration. Can enable/disable services, and " +
															   "control their roles. For plugins, can enable/disable individual plugin " +
															   "entries.";
	
	public static final String INVENTORY_ADMIN = "inventoryAdmin";
	public static final String INVENTORY_ADMIN_DESCRIPTION = "Role to enable inventory administration. Can import/export inventory data.";
	
	/*
		inventory roles
	 */
	public static final String INVENTORY_VIEW = "inventoryView";
	public static final String INVENTORY_VIEW_DESCRIPTION = "Role to enable viewing inventory.";
	
	public static final String INVENTORY_EDIT = "inventoryEdit";
	public static final String INVENTORY_EDIT_DESCRIPTION = "Role to enable editing inventory.";
	
	/**
	 * Map to easily associate roles with their description.
	 */
	public static final Map<String, String> ROLE_DESCRIPTION_MAP = Collections.unmodifiableMap(new LinkedHashMap<>() {{
		this.put(USER, USER_DESCRIPTION);
		this.put(EXT_SERVICE, EXT_SERVICE_DESCRIPTION);
		
		this.put(USER_ADMIN, USER_ADMIN_DESCRIPTION);
		this.put(EXT_SERVICE_ADMIN, EXT_SERVICE_ADMIN_DESCRIPTION);
		this.put(INVENTORY_ADMIN, INVENTORY_ADMIN_DESCRIPTION);
		
		this.put(INVENTORY_VIEW, INVENTORY_VIEW_DESCRIPTION);
		this.put(INVENTORY_EDIT, INVENTORY_EDIT_DESCRIPTION);
	}});
	
}
