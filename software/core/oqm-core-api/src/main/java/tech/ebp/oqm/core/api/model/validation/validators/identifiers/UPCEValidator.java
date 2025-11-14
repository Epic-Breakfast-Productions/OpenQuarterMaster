package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCE;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.general.upc.UpcCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class UPCEValidator extends Validator<ValidUPCE, String> {
	
	@Override
	public boolean isValid(String upceCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (!UpcCodeUtilities.isValidUPCECode(upceCode)) {
			errs.add("Invalid UPC-E code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
