package com.ebp.openQuarterMaster.lib.core.history;

import com.ebp.openQuarterMaster.lib.core.testUtils.ObjectValidationTest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.util.HashMap;
import java.util.stream.Stream;

@Slf4j
class HistoryEventValidationTest extends ObjectValidationTest<HistoryEvent> {
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(
				HistoryEvent.builder().type(EventType.ADD).build()
			)
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new HistoryEvent(),
				new HashMap<String, String>() {{
					put("type", "must not be null");
				}}
			)
		);
	}
}