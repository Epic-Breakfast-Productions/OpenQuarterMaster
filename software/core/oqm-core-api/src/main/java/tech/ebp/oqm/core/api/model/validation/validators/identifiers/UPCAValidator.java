package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCA;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.upc.UpcCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class UPCAValidator extends Validator<ValidUPCA, String> {
	
	@Override
	public boolean isValid(String upcaCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (!UpcCodeUtilities.isValidUPCACode(upcaCode)) {
			errs.add("Invalid UPCA code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
