package com.ebp.openQuarterMaster.lib.core.validation.annotations;

import com.ebp.openQuarterMaster.lib.core.validation.validators.Base64Validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check the validity of units.
 * <p>
 * https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 */
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = Base64Validator.class)
@Documented
public @interface ValidBase64 {
    String message() default "String was not base-64 encoded.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
