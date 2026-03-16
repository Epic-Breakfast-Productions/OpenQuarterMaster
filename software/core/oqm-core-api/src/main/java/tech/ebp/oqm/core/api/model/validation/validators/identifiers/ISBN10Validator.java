package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidISBN10;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.upc.ISBNCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class ISBN10Validator extends Validator<ValidISBN10, String> {
	
	@Override
	public boolean isValid(String upceCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (!ISBNCodeUtilities.isValidISBN10Code(upceCode)) {
			errs.add("Invalid ISBN-10 code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
