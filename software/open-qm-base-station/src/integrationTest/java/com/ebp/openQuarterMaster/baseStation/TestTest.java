package com.ebp.openQuarterMaster.baseStation;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusIntegrationTest
public class TestTest {

	@Test
	public void testGetConfig(){
		log.info("Auth mode: {}", ConfigProvider.getConfig().getValue("service.authMode", String.class));
	}
}
