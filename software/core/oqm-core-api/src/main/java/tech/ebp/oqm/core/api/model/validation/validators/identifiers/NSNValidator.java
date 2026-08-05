package tech.ebp.oqm.core.api.model.validation.validators.identifiers;

import jakarta.validation.ConstraintValidatorContext;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidNSN;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCA;
import tech.ebp.oqm.core.api.model.validation.validators.Validator;
import tech.ebp.oqm.core.api.service.identifiers.upc.NsnCodeUtilities;
import tech.ebp.oqm.core.api.service.identifiers.upc.UpcCodeUtilities;

import java.util.ArrayList;
import java.util.List;

public class NSNValidator extends Validator<ValidNSN, String> {

	@Override
	public boolean isValid(String nsnCode, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();

		if (nsnCode != null && !NsnCodeUtilities.isValidNsnCode(nsnCode)) {
			errs.add("Invalid NSN code.");
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
