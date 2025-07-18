package tech.ebp.oqm.core.api.model.rest.auth.externalService;

import org.bson.types.ObjectId;
import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.core.api.model.testUtils.ObjectSerializationTest;

import java.util.stream.Stream;

//class ExternalServiceLoginRequestSerializationTest extends ObjectSerializationTest<ExternalServiceLoginRequest> {
//
//	protected ExternalServiceLoginRequestSerializationTest() {
//		super(ExternalServiceLoginRequest.class);
//	}
//
//	public static Stream<Arguments> getObjects() {
//		return Stream.of(
//			Arguments.of(
//				ExternalServiceLoginRequest.builder().id(ObjectId.get()).setupToken(FAKER.random().hex()).build()
//			),
//			Arguments.of(
//				ExternalServiceSetupResponse.builder()
//											.id(ObjectId.get())
//											.setupToken(FAKER.random().hex())
//											.build().toLoginRequest()
//			)
//		);
//	}
//}