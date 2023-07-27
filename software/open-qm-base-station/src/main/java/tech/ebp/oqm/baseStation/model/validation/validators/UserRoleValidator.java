package tech.ebp.oqm.baseStation.model.validation.validators;

import tech.ebp.oqm.baseStation.model.rest.auth.roles.UserRoles;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidUserRole;
import tech.ebp.oqm.baseStation.model.validation.validators.Validator;

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
