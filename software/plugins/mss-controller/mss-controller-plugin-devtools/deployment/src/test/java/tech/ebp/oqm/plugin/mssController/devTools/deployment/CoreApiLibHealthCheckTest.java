package tech.ebp.oqm.plugin.mssController.devTools.deployment;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.Constants;

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
		ValidatableResponse response = RestAssured.when().get("/q/health")
			.then();
		
		System.out.println(response.extract().body().asPrettyString());
		
		response.statusCode(200)
			.body("status", CoreMatchers.equalTo("UP"));
	}
}
