package tech.ebp.oqm.baseStation.model.validation.annotations;

import tech.ebp.oqm.baseStation.model.validation.validators.ServiceRoleValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
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
@Target({ElementType.TYPE_USE, FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ServiceRoleValidator.class)
@Documented
public @interface ValidServiceRole {
	
	String message() default "Role is not allowed for service.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
