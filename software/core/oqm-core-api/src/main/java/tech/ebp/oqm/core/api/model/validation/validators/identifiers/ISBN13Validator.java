package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidISBN13;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.general.upc.ISBNCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class ISBN13Validator extends Validator<ValidISBN13, String> {
	
	@Override
	public boolean isValid(String upceCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (!ISBNCodeUtilities.isValidISBN13Code(upceCode)) {
			errs.add("Invalid ISBN-13 code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
