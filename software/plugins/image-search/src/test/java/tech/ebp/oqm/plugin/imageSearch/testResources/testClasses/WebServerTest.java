package tech.ebp.oqm.plugin.imageSearch.testResources.testClasses;

import net.datafaker.Faker;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
public abstract class WebServerTest {
	public static final Faker FAKER = new Faker();
}
