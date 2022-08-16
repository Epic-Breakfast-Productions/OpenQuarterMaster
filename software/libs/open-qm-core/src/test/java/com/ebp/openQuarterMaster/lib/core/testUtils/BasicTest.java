package com.ebp.openQuarterMaster.lib.core.testUtils;

import com.ebp.openQuarterMaster.lib.core.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;

public abstract class BasicTest {
	
	public static final Faker FAKER = Faker.instance();
	public static final ObjectMapper OBJECT_MAPPER = Utils.OBJECT_MAPPER;
}
