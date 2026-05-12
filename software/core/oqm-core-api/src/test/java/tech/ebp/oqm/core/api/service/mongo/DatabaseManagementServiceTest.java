package tech.ebp.oqm.core.api.service.mongo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.rest.management.CollectionClearResult;
import tech.ebp.oqm.core.api.model.rest.management.DbClearResult;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class DatabaseManagementServiceTest extends RunningServerTest {
	
	@Inject
	OqmDatabaseService oqmDatabaseService;
	
	@Inject
	DatabaseManagementService dbMgmtService;
	
	@Test
	public void testClearEmptyDb(){
		User user = this.getTestUserService().getTestUser();
		
		DbClearResult result;
		try (MongoSessionWrapper csw = new MongoSessionWrapper(null, this.oqmDatabaseService)) {
			result = this.dbMgmtService.clearDb(csw.getClientSession(), DEFAULT_TEST_DB_NAME, user);
		}
		
		assertNotNull(result);
		assertEquals(DEFAULT_TEST_DB_NAME, result.getDbName());
		assertEquals(this.oqmDatabaseService.getOqmDatabase(DEFAULT_TEST_DB_NAME).getDbId(), result.getDbId());
		
		assertEquals(7, result.getCollectionClearResults().size());
		
		for(CollectionClearResult collectionClearResult : result.getCollectionClearResults()){
			assertNotNull(collectionClearResult.getCollectionName());
			assertEquals(0, collectionClearResult.getNumRecordsDeleted());
		}
	}
	
	@Test
	public void testClearAllDbsEmpty() throws Exception {
		User user = this.getTestUserService().getTestUser();
		
		List<DbClearResult> results = this.dbMgmtService.clearAllDbs(user);
		
		for(DbClearResult result : results){
			assertNotNull(result);
			assertEquals(DEFAULT_TEST_DB_NAME, result.getDbName());
			assertEquals(this.oqmDatabaseService.getOqmDatabase(DEFAULT_TEST_DB_NAME).getDbId(), result.getDbId());
			
			assertEquals(7, result.getCollectionClearResults().size());
			
			for(CollectionClearResult collectionClearResult : result.getCollectionClearResults()){
				assertNotNull(collectionClearResult.getCollectionName());
				assertEquals(0, collectionClearResult.getNumRecordsDeleted());
			}
		}
	}
	
	//TODO:: populated dbs
}