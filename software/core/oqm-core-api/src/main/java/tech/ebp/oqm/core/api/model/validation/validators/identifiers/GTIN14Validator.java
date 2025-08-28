package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidGTIN14;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.general.upc.GTINCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class GTIN14Validator extends Validator<ValidGTIN14, String> {
	
	@Override
	public boolean isValid(String upceCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (!GTINCodeUtilities.isValidGTIN14Code(upceCode)) {
			errs.add("Invalid GTIN-14 code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
