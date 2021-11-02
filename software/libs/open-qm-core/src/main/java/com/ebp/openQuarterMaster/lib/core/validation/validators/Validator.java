package com.ebp.openQuarterMaster.lib.core.validation.validators;

import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class Validator {
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
