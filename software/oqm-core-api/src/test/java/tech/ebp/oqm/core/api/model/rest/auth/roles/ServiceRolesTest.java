package tech.ebp.oqm.core.api.model.rest.auth.roles;

import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.rest.auth.roles.ServiceRoles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceRolesTest {
	
	@Test
	public void testSelectableValid() {
		List<String> selectable = ServiceRoles.SELECTABLE_ROLES;
		
		for (String curRole : selectable) {
			assertTrue(ServiceRoles.roleAllowed(curRole), "Role in selectable list (" + curRole + ") not allowed for user.");
		}
	}
	
}