package tech.ebp.oqm.baseStation.model.validation.validators;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.model.testUtils.BasicTest;

@Slf4j
public class PasswordConstraintValidatorTest extends BasicTest {
	
	@Test
	public void testGetHtmlDescription() {
		log.info(
			"Got html: \n{}",
			PasswordConstraintValidator.getPasswordRulesDescriptionHtml()
		);
	}
	
}
