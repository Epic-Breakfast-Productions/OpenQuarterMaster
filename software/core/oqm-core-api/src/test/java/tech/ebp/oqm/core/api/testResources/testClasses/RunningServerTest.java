package tech.ebp.oqm.core.api.testResources.testClasses;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.smallrye.reactive.messaging.kafka.companion.ConsumerBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;
import tech.ebp.oqm.core.api.testResources.data.MongoTestConnector;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
public abstract class RunningServerTest extends WebServerTest {

	@Getter
	TestUserService testUserService = TestUserService.getInstance();
	
	public boolean isIntTest(){
		return getClass().isAnnotationPresent(QuarkusIntegrationTest.class);
	}

	@BeforeEach
	public void beforeEach(TestInfo testInfo){
		log.info("Running before method for test {}", testInfo.getDisplayName());
		
		User adminUser = this.getTestUserService().getTestUser(true);
		setupJwtCall(given(), this.getTestUserService().getUserToken(adminUser))
			.basePath("")
			.get("/api/v1/inventory/manage/db/refreshCache").then().statusCode(200);
		setupJwtCall(given(), this.getTestUserService().getUserToken(adminUser))
			.basePath("")
			.put("/api/v1/inventory/manage/db/ensure/" + DEFAULT_TEST_DB_NAME).then().statusCode(200);
		
		//clear kafka queues
		if(this instanceof KafkaTest){
			((KafkaTest)this).clearKafkaQueues(log);
		}
	}
	
	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());
		
		if(ConfigProvider.getConfig().getOptionalValue("quarkus.mongodb.connection-string", String.class).isEmpty()){
			log.info("Mongo not started.");
		} else {
			MongoTestConnector.getInstance(this.isIntTest()).clearDb();
		}
		
		//clear kafka queues
		if(this instanceof KafkaTest){
			((KafkaTest)this).clearKafkaQueues(log);
		}
		
		log.info("Completed after step.");
	}
}
