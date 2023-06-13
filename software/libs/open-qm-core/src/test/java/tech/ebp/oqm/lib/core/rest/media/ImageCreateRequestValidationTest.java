package tech.ebp.oqm.lib.core.rest.media;

import tech.ebp.oqm.lib.core.object.ObjectUtils;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import tech.ebp.oqm.lib.core.testUtils.ObjectValidationTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.provider.Arguments;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * TODO:: make other validator tests use this real validator as opposed to how they currently operate
 */
@Slf4j
class ImageCreateRequestValidationTest extends ObjectValidationTest<ImageCreateRequest> {
	
	private static final Base64.Encoder ENCODER = Base64.getEncoder();
	
	public static Stream<Arguments> getValid() {
		return Stream.of(
			Arguments.of(new ImageCreateRequest(
				BasicTest.FAKER.animal().name(),
				BasicTest.FAKER.lorem().sentence(),
				"test",
				new String(ENCODER.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8))),
				new ArrayList<>(),
				new HashMap<>()
			)),
			Arguments.of(new ImageCreateRequest(
				BasicTest.FAKER.animal().name(),
				BasicTest.FAKER.lorem().sentence(),
				"test",
				"data:image/png;base64," + new String(ENCODER.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8))),
				new ArrayList<>(),
				new HashMap<>()
			))
		);
	}
	
	public static Stream<Arguments> getInvalid() throws JsonProcessingException {
		return Stream.of(
			Arguments.of(
				new ImageCreateRequest(
					BasicTest.FAKER.animal().name(),
					BasicTest.FAKER.lorem().sentence(),
					"test",
					"foo\\bard" + new String(ENCODER.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8))),
					new ArrayList<>(),
					new HashMap<>()
				),
				new HashMap<>() {{
					put("imageData", "must match.*");
				}}
			),
			Arguments.of(
				ObjectUtils.OBJECT_MAPPER.readValue(
					"{" +
					"   \"title\": \"\"," +
					"   \"description\": \"" +
					BasicTest.FAKER.lorem().sentence() +
					"\"," +
					"   \"imageData\": \"" +
					new String(ENCODER.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8))) +
					"\"," +
					"   \"keywords\": []," +
					"   \"attributes\": {}" +
					"}",
					ImageCreateRequest.class
				),
				new HashMap<>() {{
					put("title", "must not be blank");
				}}
			),
			Arguments.of(
				ObjectUtils.OBJECT_MAPPER.readValue(
					"{" +
					"   \"title\": \" \"," +
					"   \"description\": \"" + BasicTest.FAKER.lorem().sentence() + "\"," +
					"   \"imageData\": \"" +
					new String(ENCODER.encode(BasicTest.FAKER.lorem().sentence().getBytes(StandardCharsets.UTF_8))) +
					"\"," +
					"   \"keywords\": []," +
					"   \"attributes\": {}" +
					"}",
					ImageCreateRequest.class
				),
				new HashMap<>() {{
					put("title", "must not be blank");
				}}
			)
		);
	}
}