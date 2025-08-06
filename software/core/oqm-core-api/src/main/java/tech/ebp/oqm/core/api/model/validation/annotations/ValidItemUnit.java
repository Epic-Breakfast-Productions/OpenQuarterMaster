package tech.ebp.oqm.core.api.model.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import tech.ebp.oqm.core.api.model.validation.validators.ItemUnitValidator;
import tech.ebp.oqm.core.api.model.validation.validators.UnitValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check the validity of units.
 * <p>
 * https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 */
@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ItemUnitValidator.class)
@Documented
public @interface ValidItemUnit {
	
	String message() default "Unit was not one of allowed units.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
