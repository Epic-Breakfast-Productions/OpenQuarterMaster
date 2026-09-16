package tech.ebp.oqm.core.api.model.validation.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class Validator<A extends Annotation, T> implements ConstraintValidator<A, T> {
	
	protected boolean processValidationResults(List<String> validationErrors, ConstraintValidatorContext context) {
		if (validationErrors == null || validationErrors.isEmpty()) {
			return true;
		}
		if (context != null) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
				String.join("; ", validationErrors)
			).addConstraintViolation();
		}
		return false;
	}
}
