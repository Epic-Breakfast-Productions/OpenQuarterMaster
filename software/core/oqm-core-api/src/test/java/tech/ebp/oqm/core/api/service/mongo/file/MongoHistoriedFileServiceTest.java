package tech.ebp.oqm.core.api.service.mongo.file;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.service.mongo.utils.FileContentsGet;
import tech.ebp.oqm.core.api.testResources.data.TestMainFileObject;
import tech.ebp.oqm.core.api.testResources.data.TestMainFileObjectGet;
import tech.ebp.oqm.core.api.testResources.data.TestMongoHistoriedFileService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;

import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
@QuarkusTest
class MongoHistoriedFileServiceTest extends RunningServerTest {
	
	@Inject
	TestMongoHistoriedFileService testMongoFileService;
	
	File testFileOne = new File(getClass().getResource("/testFiles/shakespeare.txt").getFile());
	File testFileTwo = new File(getClass().getResource("/testFiles/originOfSpecies.txt").getFile());
	File testFileNoExt = new File(getClass().getResource("/testFiles/genericFile").getFile());
	
	
	@Test
	public void testAddFile() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		assertEquals(0, this.testMongoFileService.count(DEFAULT_TEST_DB_NAME));
		
		TestMainFileObject testMainObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		ObjectId objectId = this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			testMainObject,
			testFileOne,
			testUser
		).getId();
		
		assertEquals(1, this.testMongoFileService.count(DEFAULT_TEST_DB_NAME));
		assertNotNull(testMainObject.getId());
		assertEquals(objectId, testMainObject.getId());
		assertEquals(objectId.toHexString(), testMainObject.getGridfsFileName());
		
		String fileNameWoExt = FilenameUtils.removeExtension( testMainObject.getGridfsFileName());
		assertEquals(objectId, new ObjectId(fileNameWoExt));
		
		log.info("Successfully saved file as {}: {}", testMainObject.getGridfsFileName(), testMainObject);
	}
	
	@Test
	public void testGetFileObject() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		TestMainFileObject gotten = this.testMongoFileService.getObject(DEFAULT_TEST_DB_NAME, mainFileObject.getId());
		assertEquals(mainFileObject, gotten);
	}
	
	@Test
	public void testGetFileObjectNotFound() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoFileService.getObject(DEFAULT_TEST_DB_NAME, new ObjectId()));
	}
	
	
	@Test
	public void testUpdateFile() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileTwo);
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			DEFAULT_TEST_DB_NAME,
			mainFileObject.getId(),
			this.testFileTwo,
			testUser
		);
		
		FileMetadata gotten = this.testMongoFileService.getFileMetadata(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), 2);
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	@Test
	public void testGetRevisions() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			DEFAULT_TEST_DB_NAME,
			mainFileObject.getId(),
			this.testFileTwo,
			testUser
		);
		
		List<FileMetadata> metadataList = this.testMongoFileService.getRevisions(DEFAULT_TEST_DB_NAME, mainFileObject.getId());
		log.info("Metadata list: {}", metadataList);
		
		assertEquals(2, metadataList.size());
	}
	
	@Test
	public void testGetRevisionsMulti() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		List<FileMetadata> metadataList = this.testMongoFileService.getRevisions(DEFAULT_TEST_DB_NAME, mainFileObject.getId());
		log.info("Metadata list: {}", metadataList);
		
		assertEquals(1, metadataList.size());
	}
	
	@Test
	public void testGetLatestMetadataOneRev() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileOne);
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileMetadata gotten = this.testMongoFileService.getFileMetadata(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), 1);
		
		Comparator<ZonedDateTime> comparator = Comparator.comparing(
			zdt -> zdt.truncatedTo(ChronoUnit.MINUTES)
		);
		assertEquals(0, comparator.compare(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	@Test
	public void testGetLatestMetadataTwoRev() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileTwo);
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			DEFAULT_TEST_DB_NAME,
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		FileMetadata gotten = this.testMongoFileService.getFileMetadata(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), 2);
		
		assertEquals(0, ChronoUnit.MINUTES.between(expected.getUploadDateTime(), gotten.getUploadDateTime()), "Unexpected upload datetime");
		
		gotten.setUploadDateTime(expected.getUploadDateTime());
		
		assertEquals(expected, gotten);
	}
	
	
	@Test
	public void testGetLatestMetadataNotFound() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		assertThrows(DbNotFoundException.class, ()->this.testMongoFileService.getFileMetadata(DEFAULT_TEST_DB_NAME, null, new ObjectId(), 1));
	}
	
	@Test
	public void testGetFileOneRev() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoFileService.getFile(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), 1);
		
		assertTrue(FileUtils.contentEquals(testFileOne, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testGetFileTwoRev() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			DEFAULT_TEST_DB_NAME,
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoFileService.getFile(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), 2);
		assertTrue(FileUtils.contentEquals(testFileTwo, fileGet.getContents()), "File contents were not identical");
		fileGet = this.testMongoFileService.getFile(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), 1);
		assertTrue(FileUtils.contentEquals(testFileOne, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testGetRevisionsOneRev() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expected = new FileMetadata(this.testFileOne);
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		List<FileMetadata> gotten = this.testMongoFileService.getRevisions(DEFAULT_TEST_DB_NAME, mainFileObject.getId());
		
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
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		FileMetadata expectedOne = new FileMetadata(this.testFileOne);
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileOne,
			testUser
		);
		
		FileMetadata expectedTwo = new FileMetadata(this.testFileTwo);
		this.testMongoFileService.updateFile(
			DEFAULT_TEST_DB_NAME,
			mainFileObject.getId(),
			testFileTwo,
			testUser
		);
		
		List<FileMetadata> gotten = this.testMongoFileService.getRevisions(DEFAULT_TEST_DB_NAME, mainFileObject.getId());
		
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
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			testFileNoExt,
			testUser
		);
		
		FileContentsGet fileGet = this.testMongoFileService.getFile(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), 1);
		
		assertTrue(FileUtils.contentEquals(testFileNoExt, fileGet.getContents()), "File contents were not identical");
	}
	
	@Test
	public void testRemove() throws IOException {
		User testUser = this.getTestUserService().getTestUser();
		
		TestMainFileObject mainFileObject = new TestMainFileObject(FAKER.lorem().paragraph());
		
		this.testMongoFileService.add(
			DEFAULT_TEST_DB_NAME,
			mainFileObject,
			this.testFileOne,
			testUser
		);
		
		this.testMongoFileService.updateFile(
			DEFAULT_TEST_DB_NAME,
			mainFileObject.getId(),
			this.testFileTwo,
			testUser
		);
		
		List<FileMetadata> metadataList = this.testMongoFileService.getRevisions(DEFAULT_TEST_DB_NAME, mainFileObject.getId());
		log.info("Metadata list: {}", metadataList);
		
		TestMainFileObjectGet mainFileObjectGet = this.testMongoFileService.fileObjToGet(DEFAULT_TEST_DB_NAME, mainFileObject);
		TestMainFileObjectGet result = this.testMongoFileService.removeFile(DEFAULT_TEST_DB_NAME, null, mainFileObject.getId(), testUser);
		
		assertEquals(mainFileObjectGet, result);
		assertEquals(0, this.testMongoFileService.getFileObjectService().count(DEFAULT_TEST_DB_NAME));
		assertNull(this.testMongoFileService.getGridFSBucket(DEFAULT_TEST_DB_NAME).find().first());
	}
}