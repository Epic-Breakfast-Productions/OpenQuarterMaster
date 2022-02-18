package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TestMongoService extends MongoService<TestMainObject> {
	
	TestMongoService() {//required for DI
		super(null, null, null, null, null, false, null);
	}
	
	@Inject
	TestMongoService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			TestMainObject.class,
			false
		);
	}
	
	TestMongoService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		boolean allowNullUserForCreate
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			TestMainObject.class,
			allowNullUserForCreate
		);
	}
}
