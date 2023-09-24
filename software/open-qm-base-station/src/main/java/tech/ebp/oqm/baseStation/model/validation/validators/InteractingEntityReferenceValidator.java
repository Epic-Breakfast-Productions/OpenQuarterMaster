package tech.ebp.oqm.baseStation.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityReference;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidInteractingEntityReference;

import java.util.ArrayList;
import java.util.List;

import static tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType.BASE_STATION;

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
