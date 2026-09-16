package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidEAN8;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.upc.EANCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class EAN8Validator extends Validator<ValidEAN8, String> {
	
	@Override
	public boolean isValid(String upceCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (!EANCodeUtilities.isValidEAN8Code(upceCode)) {
			errs.add("Invalid EAN-8 code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
