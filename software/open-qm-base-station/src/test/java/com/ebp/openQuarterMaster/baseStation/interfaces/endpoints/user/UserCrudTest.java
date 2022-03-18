package com.ebp.openQuarterMaster.baseStation.interfaces.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
@TestHTTPEndpoint(UserCrud.class)
class UserCrudTest extends RunningServerTest {

}