package tech.ebp.oqm.lib.core.object.storage.storageBlock;

import tech.ebp.oqm.lib.core.object.ObjectUtils;
import tech.ebp.oqm.lib.core.units.OqmProvidedUnits;
import tech.ebp.oqm.lib.core.testUtils.BasicTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class StorageBlockTest extends BasicTest {
	
	private static Stream<Arguments> jsonTestArgs() {
		//TODO:: test with ObjectIds
		return Stream.of(
			Arguments.of(
				new StorageBlock(
					FAKER.name().name(),
					"",
					"",
					FAKER.address().fullAddress(),
					null,
					new ArrayList<>() {{
						add(Quantities.getQuantity(5, OqmProvidedUnits.UNIT));
					}},
					new ArrayList<>()
				)
			),
			Arguments.of(
				new StorageBlock(
					FAKER.name().name(),
					"",
					"",
					FAKER.address().fullAddress(),
					null,
					new ArrayList<>() {{
						add(Quantities.getQuantity(5, Units.KILOGRAM));
					}},
					new ArrayList<>()
				)
			)
		);
	}
	
	/**
	 * TODO:: move to serialization test
	 * @param testStored
	 * @throws JsonProcessingException
	 */
	@ParameterizedTest(name = "jsonTest[{index}]")
	@MethodSource("jsonTestArgs")
	public void jsonTest(StorageBlock testStored) throws JsonProcessingException {
		String storedJson = ObjectUtils.OBJECT_MAPPER.writeValueAsString(testStored);
		
		log.info("Storage block object: {}", testStored);
		log.info("Storage block json: {}", storedJson);
		
		StorageBlock deserialized = ObjectUtils.OBJECT_MAPPER.readValue(storedJson, StorageBlock.class);
		
		assertEquals(testStored, deserialized, "Deserialized object was not equal to original.");
	}
}