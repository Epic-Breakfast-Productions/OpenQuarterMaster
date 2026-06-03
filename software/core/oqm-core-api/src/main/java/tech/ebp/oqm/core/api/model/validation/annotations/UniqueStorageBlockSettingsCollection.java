package tech.ebp.oqm.core.api.model.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import tech.ebp.oqm.core.api.model.validation.validators.UniqueLabeledCollectionValidator;
import tech.ebp.oqm.core.api.model.validation.validators.UniqueStorageBlockSettingsCollectionValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom annotation to check that storage block settings are unique to what storage blocks they associate.
 */
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueStorageBlockSettingsCollectionValidator.class)
@Documented
public @interface UniqueStorageBlockSettingsCollection {

	String message() default "Duplicative storage block settings were found.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
