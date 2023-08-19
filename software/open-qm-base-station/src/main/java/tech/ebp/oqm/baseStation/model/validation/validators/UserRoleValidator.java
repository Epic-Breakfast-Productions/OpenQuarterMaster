package tech.ebp.oqm.baseStation.model.validation.validators;

import tech.ebp.oqm.baseStation.model.rest.auth.roles.UserRoles;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidUserRole;
import tech.ebp.oqm.baseStation.model.validation.validators.Validator;

import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class UserRoleValidator extends Validator<ValidUserRole, String> {
	
	@Override
	public boolean isValid(String role, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (role == null) {
			errs.add("Invalid user role, was null.");
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
