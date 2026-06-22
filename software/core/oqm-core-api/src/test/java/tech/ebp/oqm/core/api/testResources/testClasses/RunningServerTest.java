package tech.ebp.oqm.core.api.testResources.testClasses;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.testResources.data.MongoTestConnector;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;

import static io.restassured.RestAssured.given;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;
import static tech.ebp.oqm.core.api.testResources.TestRestUtils.setupJwtCall;

@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
public abstract class RunningServerTest extends WebServerTest {

	private boolean needDbReset = true;
	private boolean needKafkaReset = true;

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

		log.info("\n\n======================================================\nBeginning test {}\n======================================================\n", testInfo.getDisplayName());
	}

	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("\n\n======================================================\nRunning after method for test {}\n======================================================\n", testInfo.getDisplayName());

		if(this.needDbReset){
			if(ConfigProvider.getConfig().getOptionalValue("quarkus.mongodb.connection-string", String.class).isEmpty()){
				log.info("Mongo not started.");
			} else {
				MongoTestConnector.getInstance(this.isIntTest()).clearDb();
			}
		} else {
			log.info("Skipping db reset.");
		}

		if(this.needKafkaReset) {
			//clear kafka queues
			if (this instanceof KafkaTest) {
				((KafkaTest) this).clearKafkaQueues(log);
			}
		} else {
			log.info("Skipping kafka reset.");
		}

		log.info("Completed after step.");
	}

	protected void setNeedDbReset(boolean needDbReset){
		this.needDbReset = needDbReset;
	}
	protected void setNeedKafkaReset(boolean needKafkaReset){
		this.needKafkaReset = needKafkaReset;
	}

	protected void setNeedResets(boolean needReset){
		this.setNeedDbReset(needReset);
		this.setNeedKafkaReset(needReset);
	}

}
