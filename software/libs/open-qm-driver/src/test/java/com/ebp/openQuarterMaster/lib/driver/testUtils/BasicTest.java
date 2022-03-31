package com.ebp.openQuarterMaster.lib.driver.testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

public abstract class BasicTest {
	
	public static final Faker FAKER = Faker.instance();
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
