package tech.ebp.oqm.baseStation.model.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import tech.ebp.oqm.baseStation.model.validation.validators.Base64Validator;

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
@Constraint(validatedBy = Base64Validator.class)
@Documented
public @interface ValidBase64 {
	
	String message() default "String was not base-64 encoded.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
