package tech.ebp.oqm.core.api.interfaces.endpoints.info;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.interfaces.endpoints.info.GeneralInfo;

import java.util.Currency;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("integration")
@QuarkusTest
@TestHTTPEndpoint(GeneralInfo.class)
class GeneralInfoTest extends RunningServerTest {

	Currency currency = ConfigProvider.getConfig().getValue("service.ops.currency", Currency.class);
	
	@Test
	public void testGetCurrency(){
		ValidatableResponse response = given()
			.get("currency")
			.then()
			.statusCode(200);
		
		assertEquals(
			this.currency,
			response.extract().as(Currency.class)
		);
	}
}