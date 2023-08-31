package tech.ebp.oqm.baseStation.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidUserRole;

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
