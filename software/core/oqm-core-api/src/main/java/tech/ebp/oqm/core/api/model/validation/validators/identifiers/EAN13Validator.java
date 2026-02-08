package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidEAN13;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.upc.EANCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class EAN13Validator extends Validator<ValidEAN13, String> {
	
	@Override
	public boolean isValid(String upceCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (!EANCodeUtilities.isValidEAN13Code(upceCode)) {
			errs.add("Invalid EAN-13 code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
