package tech.ebp.oqm.lib.core.rest.auth.roles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRoles {
	
	public static List<String> USER_ROLES = List.of(
		Roles.USER,
		
		Roles.USER_ADMIN,
		Roles.SERVICE_ADMIN,
		Roles.INVENTORY_ADMIN,
		
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT
	);
	
	public static List<String> allowedRoles() {
		return USER_ROLES;
	}
	
	public static boolean roleAllowed(String role) {
		return USER_ROLES.contains(role);
	}
	
	
	public static final List<String> SELECTABLE_ROLES = List.of(
		Roles.USER_ADMIN,
		Roles.SERVICE_ADMIN,
		Roles.INVENTORY_ADMIN,
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT
	);
	
	public static final Map<String, String> SELECTABLE_ROLES_DESC_MAP = Roles.ROLE_DESCRIPTION_MAP.entrySet().stream().filter(
		(Map.Entry<String, String> cur)->{
			return SELECTABLE_ROLES.contains(cur.getKey());
		}
	).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	
}
