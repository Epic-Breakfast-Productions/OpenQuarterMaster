package tech.ebp.oqm.lib.core.rest.auth.roles;

import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.object.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserRolesTest {
	
	
	@Test
	public void testSelectableValid() {
		List<String> selectable = UserRoles.SELECTABLE_ROLES;
		
		for (String curRole : selectable) {
			assertTrue(UserRoles.roleAllowed(curRole), "Role in selectable list (" + curRole + ") not allowed for user.");
		}
	}
	
}