package tech.ebp.oqm.core.api.interfaces.ui;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.testResources.testClasses.WebUiTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusIntegrationTest
public class UiGetTest extends WebUiTest {

	@Test
	public void testGetRoot(){
		given()
			.get("/")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML);
	}

	@Test
	public void testGetIndex(){
		given()
			.get("/index.html")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML);
	}

	@Test
	public void testIndexRootSame(){
		String indexContent = given()
			.get("/index.html")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.extract().body().asString();
		String rootContent = given()
			.get("/")
			.then()
			.statusCode(200)
			.contentType(ContentType.HTML)
			.extract().body().asString();

		assertEquals(indexContent, rootContent);
	}
}
