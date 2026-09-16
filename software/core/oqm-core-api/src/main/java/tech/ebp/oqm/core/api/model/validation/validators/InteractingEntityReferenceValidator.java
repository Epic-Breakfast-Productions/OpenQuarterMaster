package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidInteractingEntityReference;

import java.util.ArrayList;
import java.util.List;

import static tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityType.CORE_API;

public class InteractingEntityReferenceValidator extends Validator<ValidInteractingEntityReference, InteractingEntityReference> {
	
	@Override
	public boolean isValid(InteractingEntityReference reference, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (reference.getId() == null && !CORE_API.equals(reference.getType())) {
			errs.add("Null entity id given.");
		}
		//TODO:: if object id, not BASE_STATION?
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
