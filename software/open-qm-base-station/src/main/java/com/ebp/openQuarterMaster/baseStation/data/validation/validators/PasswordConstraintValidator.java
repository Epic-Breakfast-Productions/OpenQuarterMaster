package com.ebp.openQuarterMaster.baseStation.data.validation.validators;

import com.ebp.openQuarterMaster.baseStation.data.validation.annotations.ValidPassword;
import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates Passwords coming in.
 * <p>
 * List of rules described in {@link #RULES}
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Rule[] RULES = {
            new LengthRule(8, 256),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1),
            new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),
            new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false),
            new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false)
    };

    private static final PasswordValidator VALIDATOR = new PasswordValidator(RULES);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        RuleResult result = VALIDATOR.validate(new PasswordData(value));
        if (result.isValid()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                String.join(", ", VALIDATOR.getMessages(result))
        ).addConstraintViolation();
        return false;
    }
}
