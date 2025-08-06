package tech.ebp.oqm.core.api.service.scheduled;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class ExpiryProcessorTest extends RunningServerTest {
	
	
	
	//TODO
//	@Test
//	public void testExpires(){
//
//	}
	
	
	
}