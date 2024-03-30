package tech.ebp.oqm.baseStation.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.ServiceRoles;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidServiceRole;

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
