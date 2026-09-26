package tech.ebp.oqm.core.baseStation.testResources.testClasses;

import io.quarkus.test.common.http.TestHTTPResource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUser;
import tech.ebp.oqm.core.baseStation.testResources.testUsers.TestUserService;
import tech.ebp.oqm.lib.core.api.quarkus.testSupport.CoreApiLibTestDbManager;

import java.net.URL;


@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
public abstract class RunningServerTest extends WebServerTest {
	protected static final String TEST_DB = "default";

	@Getter
	@TestHTTPResource("/")
	URL index;

	protected String getCoreApiBaseUri() {
		return ConfigProvider.getConfig().getValue("oqm.core.api.baseUri", String.class);
	}

	@Getter
	private final TestUserService testUserService = TestUserService.getInstance();

	@BeforeEach
	public void beforeEach(TestInfo testInfo){
		log.info("Before test " + testInfo.getTestMethod().get().getName());
	}

	@AfterEach
	public void afterEach(
		TestInfo testInfo
	) {
		log.info("Running after method for test {}", testInfo.getDisplayName());

		TestUser user = this.getTestUserService().getTestUser();

		if(user.getJwt() != null){
			log.info("JWT found, clearing databases");

			CoreApiLibTestDbManager.clearAllDbs("Bearer " + user.getJwt());
		}

		log.info("Completed after step.");
	}
}
