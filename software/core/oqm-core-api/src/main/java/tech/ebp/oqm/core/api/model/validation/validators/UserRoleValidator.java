package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUserRole;

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
