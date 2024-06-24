package com.ebp.openQuarterMaster.testResources.testClasses;

import com.ebp.openQuarterMaster.testResources.OurTestDescription;
import com.ebp.openQuarterMaster.testResources.lifecycleManagers.SeleniumGridServerManager;
import com.ebp.openQuarterMaster.testResources.lifecycleManagers.TestResourceLifecycleManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.restassured.RestAssured.given;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
//@ExtendWith(SeleniumRecordingTriggerExtension.class)
public abstract class RunningServerTest extends WebServerTest {

//	@Getter
//	TestUserService testUserService = TestUserService.getInstance();

	@BeforeEach
	public void beforeEach(TestInfo testInfo){
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

		log.info("Completed after step.");
	}
}
