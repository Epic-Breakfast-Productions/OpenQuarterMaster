package tech.ebp.oqm.lib.core.rest.auth.roles;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserRoles {
	
	/**
	 * Roles that specifically pertain to Users.
	 */
	public static List<String> USER_ROLES = List.of(
		Roles.USER,
		
		Roles.USER_ADMIN,
		Roles.EXT_SERVICE_ADMIN,
		Roles.INVENTORY_ADMIN,
		
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT,
		Roles.INVENTORY_CHECKOUT
	);
	
	@Deprecated()
	public static List<String> allowedRoles() {
		return USER_ROLES;
	}
	
	/**
	 * Determines if the role string given is allowed for a user.
	 *
	 * @param role The role string to test.
	 *
	 * @return if the role string given is allowed for a user.
	 */
	public static boolean roleAllowed(String role) {
		return USER_ROLES.contains(role);
	}
	
	/**
	 * Roles that are allowed to be selectable from a ui for a user. "USER" omitted because all users need that role.
	 */
	public static final List<String> SELECTABLE_ROLES = List.of(
		Roles.USER_ADMIN,
		Roles.EXT_SERVICE_ADMIN,
		Roles.INVENTORY_ADMIN,
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT,
		Roles.INVENTORY_CHECKOUT
	);
	
	/**
	 * Map of roles to their descriptions, only for the selectable roles.
	 */
	public static final Map<String, String> SELECTABLE_ROLES_DESC_MAP = Collections.unmodifiableMap(
		new LinkedHashMap<>() {{
			for (String curRole : SELECTABLE_ROLES) {
				this.put(curRole, Roles.ROLE_DESCRIPTION_MAP.get(curRole));
			}
		}}
	);
}
