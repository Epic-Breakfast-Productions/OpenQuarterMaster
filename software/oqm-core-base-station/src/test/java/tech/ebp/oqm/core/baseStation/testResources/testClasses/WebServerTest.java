package tech.ebp.oqm.core.baseStation.testResources.testClasses;

import net.datafaker.Faker;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public abstract class WebServerTest {
	protected static final Faker FAKER = new Faker();
}
