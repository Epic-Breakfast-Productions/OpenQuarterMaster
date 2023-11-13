package tech.ebp.oqm.baseStation.model.rest;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.baseStation.model.testUtils.ObjectSerializationTest;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

class ErrorMessageSerializationTest extends ObjectSerializationTest<ErrorMessage> {
	
	protected ErrorMessageSerializationTest() {
		super(ErrorMessage.class);
	}
	
	//TODO:: work out how to accommodate commented out tests
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(
				new ErrorMessage()
			),
			Arguments.of(
				ErrorMessage.builder()
							.displayMessage(FAKER.lorem().paragraph())
							.build()
			),
			Arguments.of(
				ErrorMessage.builder()
							.displayMessage(FAKER.lorem().paragraph())
							.cause((Integer) 5)
							.build()
			),
			Arguments.of(
				ErrorMessage.builder()
							.displayMessage(FAKER.lorem().paragraph())
							.cause(ZonedDateTime.now())
							.build()
			)
//			,
//			Arguments.of(
//				ErrorMessage.builder()
//							.displayMessage("world")
//							.cause(new IllegalArgumentException("hello"))
//							.build()
//			),
//			Arguments.of(
//				ErrorMessage.builder()
//							.displayMessage(FAKER.lorem().paragraph())
//							.cause(new IllegalArgumentException(FAKER.lorem().paragraph()))
//							.build()
//			)
		);
	}
}