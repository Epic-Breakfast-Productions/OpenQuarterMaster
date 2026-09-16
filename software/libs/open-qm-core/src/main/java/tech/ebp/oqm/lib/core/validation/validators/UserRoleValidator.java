package tech.ebp.oqm.lib.core.validation.validators;

import tech.ebp.oqm.lib.core.rest.auth.roles.UserRoles;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUserRole;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class UserRoleValidator extends Validator<ValidUserRole, String> {
	
	@Override
	public boolean isValid(String role, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (role == null) {
			errs.add("Invalid user role, was null.");
		} else {
			if (!UserRoles.roleAllowed(role)) {
				errs.add("Invalid user role. " + role + " not usable for user.");
			}
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
