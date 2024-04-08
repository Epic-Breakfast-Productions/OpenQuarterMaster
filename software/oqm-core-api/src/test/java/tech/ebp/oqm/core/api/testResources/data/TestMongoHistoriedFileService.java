package tech.ebp.oqm.core.api.testResources.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.rest.file.FileUploadBody;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

@ApplicationScoped
public class TestMongoHistoriedFileService extends MongoHistoriedFileService<TestMainFileObject, FileUploadBody, TestMainFileObjectSearch, TestMainFileObjectGet> {
	
	public TestMongoHistoriedFileService() {
		super(TestMainFileObject.class, "testFile", false);
	}
	
	@Override
	public TestMainFileObjectGet fileObjToGet(TestMainFileObject obj) {
		return TestMainFileObjectGet.fromTestFileObject(obj, this.getRevisions(obj.getId()));
	}
}
