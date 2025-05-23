package tech.ebp.oqm.core.api.model.object.media;

import org.bson.types.ObjectId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;

import java.util.stream.Stream;

@Slf4j
class ImageSerializationTest extends ObjectSerializationTest<Image> {
	
	protected ImageSerializationTest() {
		super(Image.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				new Image()
					.setFileName(FAKER.file().fileName())
					.setSource(FAKER.name().name())
					.setDescription(FAKER.lorem().paragraph())
					.setId(new ObjectId())
			)
		);
	}
	
}