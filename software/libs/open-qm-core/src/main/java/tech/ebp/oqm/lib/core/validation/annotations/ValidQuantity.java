package tech.ebp.oqm.lib.core.validation.annotations;

import tech.ebp.oqm.lib.core.validation.validators.QuantityValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check the validity of units.
 * <p>
 * https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 */
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = QuantityValidator.class)
@Documented
public @interface ValidQuantity {
	
	String message() default "Quantity's unit was not one of allowed units.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
