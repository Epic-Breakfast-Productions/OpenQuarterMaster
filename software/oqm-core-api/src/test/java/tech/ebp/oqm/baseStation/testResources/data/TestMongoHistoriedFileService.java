package tech.ebp.oqm.baseStation.testResources.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.file.MongoHistoriedFileService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.File;

@ApplicationScoped
public class TestMongoHistoriedFileService extends MongoHistoriedFileService<TestMainFileObject, TestMainFileObjectSearch, TestMainFileObjectGet> {
	
	TestMongoHistoriedFileService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	TestMongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database,
		TempFileService tempFileService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			TestMainFileObject.class,
			false,
			tempFileService
		);
	}
	
	@Override
	public TestMainFileObjectGet fileObjToGet(TestMainFileObject obj) {
		return TestMainFileObjectGet.fromTestFileObject(obj, this.getRevisions(obj.getId()));
	}
}