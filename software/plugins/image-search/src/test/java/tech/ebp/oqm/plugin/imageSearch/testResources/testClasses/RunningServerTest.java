package tech.ebp.oqm.plugin.imageSearch.testResources.testClasses;

import io.quarkus.test.common.http.TestHTTPResource;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.plugin.imageSearch.testResources.testUsers.TestUser;
import tech.ebp.oqm.plugin.imageSearch.testResources.testUsers.TestUserService;

import java.net.URL;

import static io.restassured.RestAssured.given;


@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
public abstract class RunningServerTest extends WebServerTest {
	
	
	@RestClient
	OqmCoreApiClientService oqmCoreApiClientService;
	
	@Inject
	KcClientAuthService serviceAccountService;
	
	@Getter
	@ConfigProperty(name = "oqm.core.api.baseUri")
	String coreApiBaseUri;
	
	
	@Getter
	private final TestUserService testUserService = TestUserService.getInstance();
	
	@BeforeEach
	public void beforeEach(TestInfo testInfo) {
		log.info("Before test " + testInfo.getTestMethod().get().getName());
		//TODO:: use our own db? necessary?
		//		User adminUser = this.getTestUserService().getTestUser(true);
		//		setupJwtCall(given(), this.getTestUserService().getUserToken(adminUser))
		//			.basePath("")
		//			.get("/api/v1/inventory/manage/db/refreshCache").then().statusCode(200);
		//		setupJwtCall(given(), this.getTestUserService().getUserToken(adminUser))
		//			.basePath("")
		//			.put("/api/v1/inventory/manage/db/ensure/" + DEFAULT_TEST_DB_NAME).then().statusCode(200);
	}
	
	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());
		
		//TODO:: clear db
		// this might be required for
		//			if("intTest".equals(ConfigProvider.getConfig().getValue("quarkus.profile", String.class))){
		//				setupJwtCall(given(), this.getTestUserService().getUserToken(this.getTestUserService().getTestUser(true)))
		//					.basePath("")
		//					.delete("/api/v1/inventory/manage/db/" + DEFAULT_TEST_DB_NAME+ "/clearDb").then().statusCode(200);
		//			} else {
		//				MongoTestConnector.getInstance().clearDb();
		//			}
		
		
		// this.index.toString() + PassthroughProvider.PASSTHROUGH_API_ROOT + "/manage/db/clearAllDbs
		
		this.oqmCoreApiClientService.manageDbClearAll(this.serviceAccountService.getAuthString()).await();
		
		log.info("Completed after step.");
	}
}
