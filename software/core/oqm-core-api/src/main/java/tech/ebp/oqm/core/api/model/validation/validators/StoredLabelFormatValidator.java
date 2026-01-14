package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.codec.binary.Base64;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidBase64;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidStoredLabelFormat;

import java.util.ArrayList;
import java.util.List;

public class StoredLabelFormatValidator extends Validator<ValidStoredLabelFormat, String> {
	
	@Override
	public boolean isValid(String storedLabelFormat, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if(storedLabelFormat != null) {
			//TODO #1003
		}
		
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
