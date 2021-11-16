package com.ebp.openQuarterMaster.lib.core.validation.validators;

import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidPassword;
import org.passay.*;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates Passwords coming in.
 * <p>
 * List of rules described in {@link #DEFAULT_RULES}
 *
 * https://www.baeldung.com/registration-password-strength-and-rules
 *
 * http://www.passay.org/reference/
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Rule[] DEFAULT_RULES = {
            new LengthRule(8, 256),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1),
            new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),
            new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false),
            new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false)
    };

    private static PasswordValidator VALIDATOR = new PasswordValidator(DEFAULT_RULES);

    /**
     * Sets the internal password validator to use the given set of password rules.
     *
     * Just in case one wants to change the rules present.
     * @param rules The rules to use to validate passwords.
     */
    public synchronized static void setPasswordRules(Rule ... rules){
        VALIDATOR = new PasswordValidator(rules);
    }

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
