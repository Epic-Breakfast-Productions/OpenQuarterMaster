package tech.ebp.oqm.baseStation.model;

import tech.ebp.oqm.baseStation.model.testUtils.ObjectValidationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;

@Slf4j
class MainObjectValidationTest extends ObjectValidationTest<TestMainObject> {
	
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new TestMainObject()),
			Arguments.of(new TestMainObject(null, new HashMap<>() {{
				put("hello", "world");
			}}, new ArrayList<>())),
			Arguments.of(new TestMainObject(ObjectId.get(), new HashMap<>() {{
				put("hello", "world");
			}}, new ArrayList<>()))
		);
	}
	
	public static Stream<Arguments> getInvalid() {
		return Stream.of(
			Arguments.of(
				new TestMainObject() {{
					this.getKeywords().add("");
				}},
				new HashMap<>() {{
					put("keywords\\[0].<list element>", "must not be blank");
				}}
			)
		);
	}
}