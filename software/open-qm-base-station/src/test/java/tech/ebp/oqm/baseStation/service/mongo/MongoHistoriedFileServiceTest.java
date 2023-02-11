package tech.ebp.oqm.baseStation.service.mongo;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.testResources.data.TestMainFileObject;
import tech.ebp.oqm.baseStation.testResources.data.TestMongoHistoriedFileService;
import tech.ebp.oqm.baseStation.testResources.data.TestUserService;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.object.media.FileMetadata;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoHistoriedFileServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedFileService testMongoService;
	
	@Inject
	TestUserService testUserService;
	
	File testFile = new File(getClass().getResource("/testFiles/shakespeare.txt").getFile());
	
	
	@Test
	public void testAddFile() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		assertEquals(0, this.testMongoService.count());
		
		TestMainFileObject testMainObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		ObjectId objectId = this.testMongoService.add(
			testMainObject,
			testFile,
			testUser
		);
		
		assertEquals(1, this.testMongoService.count());
		assertNotNull(testMainObject.getId());
		assertEquals(objectId, testMainObject.getId());
		assertEquals(objectId + "." + FileNameUtils.getExtension(testFile.getName()), testMainObject.getFileName());
		
		String fileNameWoExt = FilenameUtils.removeExtension( testMainObject.getFileName());
		assertEquals(objectId, new ObjectId(fileNameWoExt));
		
		log.info("Successfully saved file as {}: {}", testMainObject.getFileName(), testMainObject);
	}
	
	@Test
	public void testGetFileObject() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoService.add(
			mainFileObject,
			testFile,
			testUser
		);
		
		TestMainFileObject gotten = this.testMongoService.getObject(mainFileObject.getId());
		assertEquals(mainFileObject, gotten);
	}
	
	@Test
	public void testGetFileObjectNotFound() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoService.add(
			mainFileObject,
			testFile,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoService.getObject(new ObjectId()));
	}
	
	@Test
	public void testGetLatestMetadata() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFile);
		
		this.testMongoService.add(
			mainFileObject,
			testFile,
			testUser
		);
		
		FileMetadata gotten = this.testMongoService.getLatestMetadata(mainFileObject.getId());
		
		assertEquals(expected, gotten);
	}
	
	
	@Test
	public void testGetLatestMetadataNotFound() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoService.add(
			mainFileObject,
			testFile,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoService.getLatestMetadata(new ObjectId()));
	}
	
}