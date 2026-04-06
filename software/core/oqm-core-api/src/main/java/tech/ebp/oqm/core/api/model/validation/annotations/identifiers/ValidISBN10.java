package tech.ebp.oqm.core.api.model.validation.annotations.identifiers;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import tech.ebp.oqm.core.api.model.validation.validators.identifiers.ISBN10Validator;
import tech.ebp.oqm.core.api.model.validation.validators.identifiers.ISBN13Validator;

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
@Constraint(validatedBy = ISBN10Validator.class)
@Documented
public @interface ValidISBN10 {
	
	String message() default "String was not a valid ISBN-10 code.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
