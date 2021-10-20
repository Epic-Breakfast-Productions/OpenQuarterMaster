package com.ebp.openQuarterMaster.lib.core.validation.annotations;

import com.ebp.openQuarterMaster.lib.core.validation.validators.StoredValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check the validity of units.
 *
 * https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 */
@Target({TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = StoredValidator.class)
@Documented
public @interface ValidStored {
    String message() default "Stored values were not set correctly.";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
