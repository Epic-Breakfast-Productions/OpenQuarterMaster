package tech.ebp.oqm.lib.moduleDriver.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;

public abstract class BasicTest {
	
	public static final Faker FAKER = Faker.instance();
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
