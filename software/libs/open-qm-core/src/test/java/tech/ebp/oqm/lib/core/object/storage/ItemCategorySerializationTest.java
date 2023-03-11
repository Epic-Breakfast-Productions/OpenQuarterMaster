package tech.ebp.oqm.lib.core.object.storage;

import org.junit.jupiter.params.provider.Arguments;
import tech.ebp.oqm.lib.core.testUtils.ObjectSerializationTest;

import java.awt.*;
import java.util.stream.Stream;

public class ItemCategorySerializationTest extends ObjectSerializationTest<ItemCategory> {
	
	protected ItemCategorySerializationTest() {
		super(ItemCategory.class);
	}
	
	public static Stream<Arguments> getObjects() {
		return Stream.of(
			Arguments.of(new ItemCategory().setName(FAKER.name().name())),
			Arguments.of(new ItemCategory().setName(FAKER.name().name()).setColor(Color.BLUE)),
			Arguments.of(new ItemCategory().setName(FAKER.name().name()).setColor(Color.RED)),
			Arguments.of(new ItemCategory().setName(FAKER.name().name()).setColor(Color.GREEN)),
			Arguments.of(new ItemCategory().setName(FAKER.name().name()).setColor(Color.YELLOW)),
			Arguments.of(new ItemCategory().setName(FAKER.name().name()).setColor(Color.BLACK)),
			Arguments.of(new ItemCategory().setName(FAKER.name().name()).setColor(Color.getHSBColor(24, 32, 16)))
		);
	}
}
