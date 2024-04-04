package tech.ebp.oqm.core.api.interfaces.ui;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

@Slf4j
@QuarkusIntegrationTest
public class TestTest extends RunningServerTest {

	@Test
	public void testGetConfig(){
		log.info("Auth mode: {}", ConfigProvider.getConfig().getValue("service.version", String.class));
	}
}
