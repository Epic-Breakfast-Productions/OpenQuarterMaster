package tech.ebp.oqm.baseStation.service.mongo.file;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.utils.FileContentsGet;
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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoHistoriedFileServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedFileService testMongoService;
	
	@Inject
	TestUserService testUserService;
	
	File testFileOne = new File(getClass().getResource("/testFiles/shakespeare.txt").getFile());
	File testFileTwo = new File(getClass().getResource("/testFiles/originOfSpecies.txt").getFile());
	
	
	@Test
	public void testAddFile() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		assertEquals(0, this.testMongoService.count());
		
		TestMainFileObject testMainObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		ObjectId objectId = this.testMongoService.add(
			testMainObject,
			testFileOne,
			testUser
		);
		
		assertEquals(1, this.testMongoService.count());
		assertNotNull(testMainObject.getId());
		assertEquals(objectId, testMainObject.getId());
		assertEquals(objectId + "." + FileNameUtils.getExtension(testFileOne.getName()), testMainObject.getFileName());
		
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
			testFileOne,
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
			testFileOne,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoService.getObject(new ObjectId()));
	}
	
	@Test
	public void testGetLatestMetadataOneRev() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileOne);
		
		this.testMongoService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileMetadata gotten = this.testMongoService.getLatestMetadata(mainFileObject.getId());
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	@Test
	public void testGetLatestMetadataTwoRev() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileTwo);
		
		this.testMongoService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		this.testMongoService.updateFile(
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		FileMetadata gotten = this.testMongoService.getLatestMetadata(mainFileObject.getId());
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	
	@Test
	public void testGetLatestMetadataNotFound() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoService.getLatestMetadata(new ObjectId()));
	}
	
	@Test
	public void testGetLatestFileOneRev() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoService.getLatestFile(mainFileObject.getId());
		
		assertTrue(FileUtils.contentEquals(testFileOne, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testGetLatestFileTwoRev() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		this.testMongoService.updateFile(
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoService.getLatestFile(mainFileObject.getId());
		
		assertTrue(FileUtils.contentEquals(testFileTwo, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testGetRevisionsOneRev() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileOne);
		
		this.testMongoService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		List<FileMetadata> gotten = this.testMongoService.getRevisions(mainFileObject.getId());
		
		assertEquals(1, gotten.size());
		
		FileMetadata gottenMd = gotten.get(0);
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gottenMd.getUploadDateTime()), "Unexpected upload datetime");
		
		gottenMd.setUploadDateTime(expected.getUploadDateTime());
		assertEquals(expected, gottenMd);
	}
	
	@Test
	public void testGetRevisionsTwoRev() throws IOException {
		User testUser = testUserService.getTestUser(true, true);
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expectedOne = new FileMetadata(this.testFileOne);
		
		this.testMongoService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileMetadata expectedTwo = new FileMetadata(this.testFileTwo);
		this.testMongoService.updateFile(
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		List<FileMetadata> gotten = this.testMongoService.getRevisions(mainFileObject.getId());
		
		assertEquals(2, gotten.size());
		
		FileMetadata gottenMdOne = gotten.get(0);
		FileMetadata gottenMdTwo = gotten.get(1);
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expectedOne.getUploadDateTime(), gottenMdOne.getUploadDateTime()), "Unexpected upload datetime");
		assertEquals(0, comparator.compare(expectedTwo.getUploadDateTime(), gottenMdTwo.getUploadDateTime()), "Unexpected upload datetime");
		
		gottenMdOne.setUploadDateTime(expectedOne.getUploadDateTime());
		gottenMdTwo.setUploadDateTime(expectedTwo.getUploadDateTime());
		assertEquals(expectedOne, gottenMdOne);
		assertEquals(expectedTwo, gottenMdTwo);
	}
	
}