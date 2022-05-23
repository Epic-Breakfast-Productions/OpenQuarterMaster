package com.ebp.openQuarterMaster.plugin;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@QuarkusIntegrationTest
public class ConfigTest {

	@ConfigProperty(name = "quarkus.mongodb.connection-string")
	String mongoConnString;
	
	@Test
	public void testConfig(){
		log.info("Mongo connection string: {}", this.mongoConnString);
		assertNotNull(this.mongoConnString);
	}
}
