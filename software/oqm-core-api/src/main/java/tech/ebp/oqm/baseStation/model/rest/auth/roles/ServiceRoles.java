package tech.ebp.oqm.baseStation.model.rest.auth.roles;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceRoles {
	
	public static List<String> SERVICE_ROLES = List.of(
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.EXT_SERVICE,
		
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_ADMIN,
		
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_VIEW,
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_EDIT,
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_CHECKOUT
	);
	
	public static List<String> allowedRoles() {
		return SERVICE_ROLES;
	}
	
	public static boolean roleAllowed(String role) {
		return SERVICE_ROLES.contains(role);
	}
	
	
	public static final List<String> SELECTABLE_ROLES = List.of(
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_ADMIN,
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_VIEW,
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_EDIT,
		tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles.INVENTORY_CHECKOUT
	);
	
	public static final Map<String, String> SELECTABLE_ROLES_DESC_MAP = Roles.ROLE_DESCRIPTION_MAP.entrySet().stream().filter(
		(Map.Entry<String, String> cur)->{
			return SELECTABLE_ROLES.contains(cur.getKey());
		}
	).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	
}