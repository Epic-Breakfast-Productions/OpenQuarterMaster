package tech.ebp.oqm.baseStation.service.mongo.file;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
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
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@Disabled("Waiting on file transaction support")//TODO::  #51
@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class MongoHistoriedFileServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedFileService testMongoFileService;
	
	@Inject
	TestUserService testUserService;
	
	File testFileOne = new File(getClass().getResource("/testFiles/shakespeare.txt").getFile());
	File testFileTwo = new File(getClass().getResource("/testFiles/originOfSpecies.txt").getFile());
	File testFileNoExt = new File(getClass().getResource("/testFiles/genericFile").getFile());
	
	
	@Test
	public void testAddFile() throws IOException {
		User testUser = testUserService.getTestUser();
		
		assertEquals(0, this.testMongoFileService.count());
		
		TestMainFileObject testMainObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		ObjectId objectId = this.testMongoFileService.add(
			testMainObject,
			testFileOne,
			testUser
		);
		
		assertEquals(1, this.testMongoFileService.count());
		assertNotNull(testMainObject.getId());
		assertEquals(objectId, testMainObject.getId());
		assertEquals(objectId.toHexString(), testMainObject.getGridfsFileName());
		
		String fileNameWoExt = FilenameUtils.removeExtension( testMainObject.getGridfsFileName());
		assertEquals(objectId, new ObjectId(fileNameWoExt));
		
		log.info("Successfully saved file as {}: {}", testMainObject.getGridfsFileName(), testMainObject);
	}
	
	@Test
	public void testGetFileObject() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		TestMainFileObject gotten = this.testMongoFileService.getObject(mainFileObject.getId());
		assertEquals(mainFileObject, gotten);
	}
	
	@Test
	public void testGetFileObjectNotFound() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoFileService.getObject(new ObjectId()));
	}
	
	
	@Test
	public void testUpdateFile() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileTwo);
		
		this.testMongoFileService.add(
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			mainFileObject.getId(),
			this.testFileTwo,
			testUser
		);
		
		FileMetadata gotten = this.testMongoFileService.getFileMetadata(null, mainFileObject.getId(), 2);
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	@Test
	public void testGetRevisions() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			mainFileObject.getId(),
			this.testFileTwo,
			testUser
		);
		
		List<FileMetadata> metadataList = this.testMongoFileService.getRevisions(mainFileObject.getId());
		log.info("Metadata list: {}", metadataList);
		
		assertEquals(2, metadataList.size());
	}
	
	@Test
	public void testGetRevisionsMulti() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		List<FileMetadata> metadataList = this.testMongoFileService.getRevisions(mainFileObject.getId());
		log.info("Metadata list: {}", metadataList);
		
		assertEquals(1, metadataList.size());
	}
	
	@Test
	public void testGetLatestMetadataOneRev() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileOne);
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileMetadata gotten = this.testMongoFileService.getFileMetadata(null, mainFileObject.getId(), 1);
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	@Test
	public void testGetLatestMetadataTwoRev() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileTwo);
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		FileMetadata gotten = this.testMongoFileService.getFileMetadata(null, mainFileObject.getId(), 2);
		
		//TODO:: compare duration between, not stamps?
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	
	@Test
	public void testGetLatestMetadataNotFound() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoFileService.getFileMetadata(null, new ObjectId(), 1));
	}
	
	@Test
	public void testGetFileOneRev() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoFileService.getFile(null, mainFileObject.getId(), 1);
		
		assertTrue(FileUtils.contentEquals(testFileOne, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testGetFileTwoRev() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoFileService.getFile(null, mainFileObject.getId(), 2);
		assertTrue(FileUtils.contentEquals(testFileTwo, fileGet.getContents()), "File contents were not identical");
		fileGet = this.testMongoFileService.getFile(null, mainFileObject.getId(), 1);
		assertTrue(FileUtils.contentEquals(testFileOne, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testGetRevisionsOneRev() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileOne);
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		List<FileMetadata> gotten = this.testMongoFileService.getRevisions(mainFileObject.getId());
		
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
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expectedOne = new FileMetadata(this.testFileOne);
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileMetadata expectedTwo = new FileMetadata(this.testFileTwo);
		this.testMongoFileService.updateFile(
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		List<FileMetadata> gotten = this.testMongoFileService.getRevisions(mainFileObject.getId());
		
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
	
	@Test
	public void testFileWithNoExt() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			testFileNoExt,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoFileService.getFile(null, mainFileObject.getId(), 1);
		
		assertTrue(FileUtils.contentEquals(testFileNoExt, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testRemove() throws IOException {
		User testUser = testUserService.getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			mainFileObject.getId(),
			this.testFileTwo,
			testUser
		);
		
		List<FileMetadata> metadataList = this.testMongoFileService.getRevisions(mainFileObject.getId());
		log.info("Metadata list: {}", metadataList);
		
		TestMainFileObject result = this.testMongoFileService.removeFile(null, mainFileObject.getId(), testUser);
		
		assertEquals(mainFileObject, result);
		assertEquals(0, this.testMongoFileService.getFileObjectService().count());
		assertNull(this.testMongoFileService.getGridFSBucket().find().first());
	}
}