package tech.ebp.oqm.baseStation.utils;

import java.util.Map;

public final class UserRoles {
	public static final String USER = "user";
	public static final String USER_ADMIN = "userAdmin";
	
	public static final String INVENTORY_VIEW = "inventoryView";
	public static final String INVENTORY_EDIT = "inventoryEdit";
	public static final String INVENTORY_ADMIN = "inventoryAdmin";
	
	public static final Map<String, String> SELECTABLE_ROLES = Map.of(
		USER_ADMIN, "Can administer users",
		INVENTORY_VIEW, "Can view inventory",
		INVENTORY_EDIT, "Can edit inventory",
		INVENTORY_ADMIN, "Can do administrative actions to inventory data, such as backups and pruning."
	);
}
