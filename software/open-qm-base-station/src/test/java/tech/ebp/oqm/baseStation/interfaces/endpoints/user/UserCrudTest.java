package tech.ebp.oqm.baseStation.interfaces.endpoints.user;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;

@Tag("integration")
@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
@TestHTTPEndpoint(UserCrud.class)
class UserCrudTest extends RunningServerTest {

}