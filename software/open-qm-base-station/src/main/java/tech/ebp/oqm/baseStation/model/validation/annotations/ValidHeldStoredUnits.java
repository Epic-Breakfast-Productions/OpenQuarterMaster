package tech.ebp.oqm.baseStation.model.validation.annotations;

import tech.ebp.oqm.baseStation.model.validation.validators.ValidStoredUnitsValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check the validity of units.
 * <p>
 * https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html#validator-customconstraints-validator
 *
 * TODO:: bring back when https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578 resolved
 */
@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ValidStoredUnitsValidator.class)
@Documented
public @interface ValidHeldStoredUnits {

	String message() default "One or more stored objects had incompatible units.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
