package tech.ebp.oqm.lib.core.validation.validators;

import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.lib.core.rest.auth.roles.ServiceRoles;
import tech.ebp.oqm.lib.core.validation.annotations.ValidInteractingEntityReference;
import tech.ebp.oqm.lib.core.validation.annotations.ValidServiceRole;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

import static tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntityType.BASE_STATION;

public class InteractingEntityReferenceValidator extends Validator<ValidInteractingEntityReference, InteractingEntityReference> {
	
	@Override
	public boolean isValid(InteractingEntityReference reference, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (reference.getEntityId() == null && !BASE_STATION.equals(reference.getEntityType())) {
			errs.add("Null entity id given.");
		}
		//TODO:: if object id, not BASE_STATION?
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
