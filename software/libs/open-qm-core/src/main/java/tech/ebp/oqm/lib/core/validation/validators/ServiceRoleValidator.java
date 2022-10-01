package tech.ebp.oqm.lib.core.validation.validators;

import tech.ebp.oqm.lib.core.rest.auth.roles.ServiceRoles;
import tech.ebp.oqm.lib.core.rest.auth.roles.UserRoles;
import tech.ebp.oqm.lib.core.validation.annotations.ValidServiceRole;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUserRole;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class ServiceRoleValidator extends Validator<ValidServiceRole, String> {
	
	@Override
	public boolean isValid(String role, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (role == null) {
			errs.add("Invalid service role, was null.");
		} else {
			if (!ServiceRoles.roleAllowed(role)) {
				errs.add("Invalid service role. " + role + " not usable for service.");
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
