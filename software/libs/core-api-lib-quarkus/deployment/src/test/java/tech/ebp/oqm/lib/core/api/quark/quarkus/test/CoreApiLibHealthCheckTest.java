package tech.ebp.oqm.lib.core.api.quark.quarkus.test;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.Constants;

public class CoreApiLibHealthCheckTest {
	
	@RegisterExtension
	static final QuarkusUnitTest config = new QuarkusUnitTest()
											  .withEmptyApplication()
													//TODO:: setting this breaks things.... why?
//												  .overrideConfigKey(Constants.CONFIG_ROOT_NAME + ".health.enabled", "true")

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
