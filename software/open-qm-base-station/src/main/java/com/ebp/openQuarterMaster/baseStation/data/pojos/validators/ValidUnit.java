package com.ebp.openQuarterMaster.baseStation.data.pojos.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.ebp.openQuarterMaster.baseStation.data.pojos.Utils.ALLOWED_MEASUREMENTS;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check the validity of units.
 *
 * https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 */
@Target({ FIELD, METHOD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = UnitValidator.class)
@Documented
public @interface ValidUnit {
    String message() default "Unit was not one of allowed units.";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
