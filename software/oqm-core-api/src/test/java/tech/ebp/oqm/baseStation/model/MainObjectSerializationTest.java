package tech.ebp.oqm.baseStation.model;

import tech.ebp.oqm.baseStation.model.testUtils.ObjectSerializationTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
class MainObjectSerializationTest extends ObjectSerializationTest<TestMainObject> {
	
	protected MainObjectSerializationTest() {
		super(TestMainObject.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new TestMainObject()),
			Arguments.of(new TestMainObject(null, new HashMap<>() {{
				put("hello", "world");
			}}, new ArrayList<>())),
			Arguments.of(new TestMainObject(ObjectId.get(), new HashMap<>() {{
				put("hello", "world");
			}}, new ArrayList<>())),
			Arguments.of(new TestMainObject(ObjectId.get(), new HashMap<>() {{
				put("hello", "world");
			}}, List.of("hello")))
		);
	}
}