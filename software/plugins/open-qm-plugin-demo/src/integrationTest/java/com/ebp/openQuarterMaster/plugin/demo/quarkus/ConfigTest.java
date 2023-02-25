package com.ebp.openQuarterMaster.plugin.demo.quarkus;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusIntegrationTest
public class ConfigTest {

	String mongoConnString = ConfigProvider.getConfig().getValue("quarkus.mongodb.connection-string", String.class);
	
	@Test
	public void testConfig(){
		log.info("Mongo connection string: {}", this.mongoConnString);
		assertNotNull(this.mongoConnString);
	}
}
