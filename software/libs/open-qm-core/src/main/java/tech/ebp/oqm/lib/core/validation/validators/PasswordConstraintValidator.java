package tech.ebp.oqm.lib.core.validation.validators;

import tech.ebp.oqm.lib.core.validation.annotations.ValidPassword;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.EnglishSequenceData;
import org.passay.IllegalSequenceRule;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;

import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

/**
 * Validates Passwords coming in.
 * <p>
 * List of rules described in {@link #DEFAULT_RULES}
 * <p>
 * https://www.baeldung.com/registration-password-strength-and-rules
 * <p>
 * http://www.passay.org/reference/
 */
public class PasswordConstraintValidator extends Validator<ValidPassword, String> {
	
	private static final Rule[] DEFAULT_RULES = {
		new LengthRule(8, 256),
		new CharacterRule(EnglishCharacterData.UpperCase, 1),
		new CharacterRule(EnglishCharacterData.LowerCase, 1),
		new CharacterRule(EnglishCharacterData.Digit, 1),
		new CharacterRule(EnglishCharacterData.Special, 1)
		//		new IllegalSequenceRule(EnglishSequenceData.Alphabetical, 5, false),//TODO:: use when we know how to display these to user
		//		new IllegalSequenceRule(EnglishSequenceData.Numerical, 5, false),
		//		new IllegalSequenceRule(EnglishSequenceData.USQwerty, 5, false)
	};
	
	private static PasswordValidator VALIDATOR = new PasswordValidator(DEFAULT_RULES);
	
	/**
	 * Sets the internal password validator to use the given set of password rules.
	 * <p>
	 * Just in case one wants to change the rules present.
	 *
	 * @param rules The rules to use to validate passwords.
	 */
	public synchronized static void setPasswordRules(Rule... rules) {
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
	
	public static String getPasswordRulesDescriptionHtml() {
		List<? extends Rule> rules = VALIDATOR.getRules();
		StringBuilder sb = new StringBuilder("Password requirements: <br />\n<ul>\n");
		
		for (Rule curRule : rules) {
			sb.append("\t<li>\n\t\t");
			
			if (curRule instanceof LengthRule) {
				sb.append("Must be between ");
				sb.append(((LengthRule) curRule).getMinimumLength());
				sb.append(" and ");
				sb.append(((LengthRule) curRule).getMaximumLength());
				sb.append(" characters in length.");
			} else if (curRule instanceof CharacterRule) {
				sb.append("Must contain at least ");
				sb.append(((CharacterRule) curRule).getNumberOfCharacters());
				sb.append(" ");
				sb.append(((CharacterRule) curRule).getCharacterData());
				sb.append(" characters: ");
				sb.append(((CharacterRule) curRule).getValidCharacters().replace("<", "&lt;").replace(">", "&gt;"));
			} else if (curRule instanceof IllegalSequenceRule) {
				sb.append("Must not contain character sequence greater than or equal to ");
				sb.append(((IllegalSequenceRule) curRule).getSequenceLength());
				sb.append(" character in: ");
				sb.append(//TODO:: do this better
						  Arrays.toString(((IllegalSequenceRule) curRule).getSequenceData().getSequences())
								.replace("<", "&lt;").replace(">", "&gt;")
				);
			} else {
				sb.append(curRule.toString());
			}
			
			sb.append("\n\t</li>\n");
		}
		
		sb.append("</ul>");
		
		return sb.toString();
	}
}
