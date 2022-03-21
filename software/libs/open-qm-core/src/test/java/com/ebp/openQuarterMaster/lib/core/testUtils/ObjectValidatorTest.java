package com.ebp.openQuarterMaster.lib.core.testUtils;

import lombok.extern.slf4j.Slf4j;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class ObjectValidatorTest<T> extends BasicTest {
	
	protected static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
	
	protected T oldValidator;
	
	protected static void assertHasErrorMessages(TestConstraintValidatorContext ctx, String... message) {
		Map<String, Boolean> foundMap = new HashMap<>();
		
		log.debug("Got error message(s): {}", ctx.getErrorMessages());
		for (String curExpected : message) {
			foundMap.put(curExpected, false);
			for (String curErr : ctx.getErrorMessages()) {
				if (curErr.contains(curExpected)) {
					foundMap.put(curExpected, true);
				}
			}
		}
		List<String> notFound = new ArrayList<>(foundMap.size());
		
		for (Map.Entry<String, Boolean> curExpected : foundMap.entrySet()) {
			if (!curExpected.getValue()) {
				notFound.add(curExpected.getKey());
			}
		}
		
		assertTrue(notFound.isEmpty(), "Error message not found: " + notFound.toString());
	}
	
}
