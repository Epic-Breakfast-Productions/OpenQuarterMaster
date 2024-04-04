package tech.ebp.oqm.core.api.model.testUtils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class ObjectValidationTest<T> extends BasicTest {
	
	protected static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	@ParameterizedTest
	@MethodSource("getValid")
	public void testValid(T object) {
		log.info("Testing object: {}", object);
		Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
		
		assertTrue(
			violations.isEmpty(),
			"Had validation errors: " + violations.toString()
		);
	}
	
	@ParameterizedTest
	@MethodSource("getInvalid")
	public void testInvalid(
		T object,
		Map<String, String> expectedFieldMessageMap
	) {
		log.info("Testing invalid object: {}", object);
		
		Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
		
		assertFalse(
			violations.isEmpty(),
			"Had no validation errors."
		);
		
		log.info("Got validation errors: {}", violations);
		
		if (expectedFieldMessageMap != null) {
			assertEquals(
				expectedFieldMessageMap.size(),
				violations.size(),
				"Different number of violations from expected."
			);
			
			List<ConstraintViolation<T>> violationList = new ArrayList<>(violations);
			for (Map.Entry<String, String> curExpected : expectedFieldMessageMap.entrySet()) {
				boolean found = false;
				
				Pattern fieldPattern = Pattern.compile(curExpected.getKey());
				Pattern messagePattern = Pattern.compile(curExpected.getValue());
				
				for (ConstraintViolation<T> curViolation : violationList) {
					if (
						fieldPattern.matcher(curViolation.getPropertyPath().toString()).matches() &&
						messagePattern.matcher(curViolation.getMessage()).matches()
					) {
						found = true;
						violationList.remove(curViolation);
						break;
					}
				}
				assertTrue(
					found,
					"Could not find expected violation message: field: \"" +
					curExpected.getKey() +
					"\"  message: \"" +
					curExpected.getValue() +
					"\""
				);
			}
		}
		
	}
	
}
