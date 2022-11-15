package tech.ebp.oqm.lib.core.rest.auth.roles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceRoles {
	
	public static List<String> SERVICE_ROLES = List.of(
		Roles.EXT_SERVICE,
		
		Roles.EXT_SERVICE_ADMIN,
		Roles.INVENTORY_ADMIN,
		
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT
	);
	
	public static List<String> allowedRoles() {
		return SERVICE_ROLES;
	}
	
	public static boolean roleAllowed(String role) {
		return SERVICE_ROLES.contains(role);
	}
	
	
	public static final List<String> SELECTABLE_ROLES = List.of(
		Roles.EXT_SERVICE_ADMIN,
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
