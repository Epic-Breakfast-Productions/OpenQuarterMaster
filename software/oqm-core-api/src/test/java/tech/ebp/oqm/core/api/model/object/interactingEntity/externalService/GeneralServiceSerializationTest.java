package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;

import java.util.stream.Stream;

@Slf4j
class GeneralServiceSerializationTest extends ObjectSerializationTest<GeneralService> {
	
	protected GeneralServiceSerializationTest() {
		super(GeneralService.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				new GeneralService()
					.setName(FAKER.name().name())
					.setDeveloperName(FAKER.name().name())
					.setDeveloperEmail(FAKER.internet().emailAddress())
					.setAuthProvider(FAKER.internet().url())
					.setIdFromAuthProvider(FAKER.idNumber().valid())
					.setId(ObjectId.get())
					
			)
		);
	}
	
}