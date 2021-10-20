package com.ebp.openQuarterMaster.lib.core.validation.annotations;

import com.ebp.openQuarterMaster.lib.core.validation.validators.InventoryItemValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check the validity of units.
 *
 * https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 */
@Target({TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = InventoryItemValidator.class)
@Documented
public @interface ValidInventoryItem {
    String message() default "Unit was not one of allowed units.";

    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
