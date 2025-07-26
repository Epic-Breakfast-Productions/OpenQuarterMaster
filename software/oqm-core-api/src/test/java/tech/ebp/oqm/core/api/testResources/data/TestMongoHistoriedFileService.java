package tech.ebp.oqm.core.api.testResources.data;

import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.service.mongo.file.MongoHistoriedFileService;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TestMongoHistoriedFileService extends MongoHistoriedFileService<TestMainFileObject, FileUploadBody, TestMainFileObjectSearch, TestMainFileObjectGet> {
	
	public TestMongoHistoriedFileService() {
		super(TestMainFileObject.class, "testFile", false);
	}
	
	@Override
	public TestMainFileObjectGet fileObjToGet(String dbNameOrId, TestMainFileObject obj) {
		return TestMainFileObjectGet.fromTestFileObject(obj, this.getRevisions(dbNameOrId, obj.getId()));
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return TestMainFileObject.CUR_SCHEMA_VERSION;
	}
}
