package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.trackedStored;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;

import java.util.stream.Stream;

class TrackedMapStoredWrapperSerealizationTest extends ObjectSerializationTest<TrackedMapStoredWrapper> {
	
	protected TrackedMapStoredWrapperSerealizationTest() {
		super(TrackedMapStoredWrapper.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new TrackedMapStoredWrapper())
		);
	}
}