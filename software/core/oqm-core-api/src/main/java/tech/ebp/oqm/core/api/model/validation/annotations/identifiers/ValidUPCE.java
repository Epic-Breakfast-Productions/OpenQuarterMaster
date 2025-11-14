package tech.ebp.oqm.core.api.model.validation.annotations.identifiers;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import tech.ebp.oqm.core.api.model.validation.validators.identifiers.UPCAValidator;
import tech.ebp.oqm.core.api.model.validation.validators.identifiers.UPCEValidator;

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
@Constraint(validatedBy = UPCEValidator.class)
@Documented
public @interface ValidUPCE {
	
	String message() default "String was not a valid UPC-E code.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
