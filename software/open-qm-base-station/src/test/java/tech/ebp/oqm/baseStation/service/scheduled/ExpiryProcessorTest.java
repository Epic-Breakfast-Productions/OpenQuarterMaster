package tech.ebp.oqm.baseStation.service.scheduled;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.profiles.ExternalAuthTestProfile;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;

@Slf4j
@QuarkusTest
@TestProfile(ExternalAuthTestProfile.class)
@QuarkusTestResource(TestResourceLifecycleManager.class)
class ExpiryProcessorTest extends RunningServerTest {
	
	
	
	//TODO
//	@Test
//	public void testExpires(){
//
//	}
	
	
	
}