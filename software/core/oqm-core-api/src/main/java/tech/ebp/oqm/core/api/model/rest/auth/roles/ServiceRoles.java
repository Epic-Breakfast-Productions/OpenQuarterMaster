package tech.ebp.oqm.core.api.model.rest.auth.roles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceRoles {
	
	public static List<String> SERVICE_ROLES = List.of(
		Roles.EXT_SERVICE,
		
		Roles.INVENTORY_ADMIN,
		
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT,
		Roles.INVENTORY_CHECKOUT
	);
	
	public static List<String> allowedRoles() {
		return SERVICE_ROLES;
	}
	
	public static boolean roleAllowed(String role) {
		return SERVICE_ROLES.contains(role);
	}
	
	
	public static final List<String> SELECTABLE_ROLES = List.of(
		Roles.INVENTORY_ADMIN,
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT,
		Roles.INVENTORY_CHECKOUT
	);
	
	public static final Map<String, String> SELECTABLE_ROLES_DESC_MAP = Roles.ROLE_DESCRIPTION_MAP.entrySet().stream().filter(
		(Map.Entry<String, String> cur)->{
			return SELECTABLE_ROLES.contains(cur.getKey());
		}
	).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	
}
