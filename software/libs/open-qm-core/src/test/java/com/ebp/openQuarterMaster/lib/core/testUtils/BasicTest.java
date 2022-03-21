package com.ebp.openQuarterMaster.lib.core.testUtils;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public abstract class BasicTest {
	
	public static final Faker FAKER = Faker.instance();
}
