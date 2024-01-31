package tech.ebp.oqm.lib.core.api.quark.quarkus.test;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

public class CoreApiLibHealthCheckTest {
	
	@RegisterExtension
	static final QuarkusUnitTest config = new QuarkusUnitTest()
											  .withEmptyApplication()
												  .overrideConfigKey("quarkus."+Constants.CONFIG_ROOT_NAME + ".health.enabled", "true")
//											  .withConfigurationResource("application-default-datasource.properties")
//											  .overrideConfigKey("quarkus.datasource.health.enabled", "true")
		;
	
	@Test
	public void testDataSourceHealthCheckExclusion() {
		RestAssured.when().get("/q/health")
			.then()
			.statusCode(200)
			.body("status", CoreMatchers.equalTo("UP"));
	}
}
