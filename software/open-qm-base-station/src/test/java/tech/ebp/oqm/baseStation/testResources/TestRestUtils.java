package tech.ebp.oqm.baseStation.testResources;

import io.restassured.http.Header;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class TestRestUtils {
	
	public static RequestSpecification setupJwtCall(RequestSpecification call, String token) {
		return call.header(new Header("Authorization", "Bearer " + token));
	}
	
	public static RequestSpecification newJwtCall(String token) {
		RequestSpecification request = given()
			.when();
		
		setupJwtCall(request, token);
		
		return request;
	}
	//
	//	public static void assertErrorMessage(String expected, String message) {
	//		if(!message.matches(expected)) {
	//			fail("Error message \"" + message + "\" did not match expected: \"" + expected + "\"");
	//		}
	//	}
	//
	//	public static void assertErrorMessage(String expected, ValidatableResponse response) {
	//		assertErrorMessage(expected, response.extract().asString());
	//	}
	//
	//	public static UserRegistrationRequest getUserRegistrationRequest(TestUser testUser) {
	//		return new UserRegistrationRequest(
	//			testUser.getUsername(),
	//			testUser.getEmail(),
	//			testUser.getPlainPassword()
	//		);
	//	}
}
