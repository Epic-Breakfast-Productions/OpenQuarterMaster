package com.ebp.openQuarterMaster.lib.core.testUtils;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

public abstract class BasicTest {
	
	public static final Faker FAKER = Faker.instance();
	public static final ObjectMapper OBJECT_MAPPER = Utils.OBJECT_MAPPER;
}
