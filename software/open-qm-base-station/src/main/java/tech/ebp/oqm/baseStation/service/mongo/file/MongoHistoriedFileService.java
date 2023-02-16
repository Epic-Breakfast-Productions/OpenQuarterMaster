package tech.ebp.oqm.baseStation.service.mongo.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.lib.core.object.FileMainObject;
import tech.ebp.oqm.lib.core.object.history.events.file.NewFileVersionEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.object.media.FileMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
@Traced
public abstract class MongoHistoriedFileService<T extends FileMainObject, S extends SearchObject<T>> extends MongoFileService<T, S> {
	
	public static final String NULL_USER_EXCEPT_MESSAGE = "User must exist to perform action.";
	
	/**
	 * TODO:: check if real user. Get userService in constructor?
	 * TODO:: real exception
	 *
	 * @param interactingEntity
	 */
	private static void assertNotNullEntity(InteractingEntity interactingEntity) {
		if (interactingEntity == null) {
			throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
		}
		//TODO:: check has id
	}
	
	@Getter
	protected final boolean allowNullEntityForCreate;
	@Getter
	private MongoHistoriedObjectService<T, S> fileObjectService = null;
	
	public MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		boolean allowNullEntityForCreate,
		MongoHistoriedObjectService<T, S> fileMetadataService
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.fileObjectService = fileMetadataService;
	}
	
	protected MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> metadataClazz,
		boolean allowNullEntityForCreate,
		TempFileService tempFileService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			metadataClazz,
			tempFileService
		);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.fileObjectService =
			new FileMetadataService(
				objectMapper,
				mongoClient,
				database,
				metadataClazz
			);
	}
	protected MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> metadataClazz,
		boolean allowNullEntityForCreate,
		TempFileService tempFileService,
		MongoHistoriedObjectService<T, S> historiedObjectService
	) {
		this(
			objectMapper,
			mongoClient,
			database,
			metadataClazz,
			allowNullEntityForCreate,
			tempFileService
		);
		this.fileObjectService = historiedObjectService;
	}
	
	
	private class FileMetadataService extends MongoHistoriedObjectService<T, S> {
		
		FileMetadataService() {//required for DI
			super(null, null, null, null, null, null, false, null);
		}
		
		FileMetadataService(
			ObjectMapper objectMapper,
			MongoClient mongoClient,
			String database,
			Class<T> clazz
		) {
			super(
				objectMapper,
				mongoClient,
				database,
				clazz,
				false
			);
			//        this.validator = validator;
		}
	}
	
	
	public ObjectId add(ClientSession clientSession, T fileObject, File file, String fileName, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		fileMetadata.setOrigName(FilenameUtils.getName(fileName));
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			return this.add(
				clientSession,
				fileObject,
				fileMetadata,
				is,
				interactingEntity
			);
		}
	}
	
	public ObjectId add(ClientSession clientSession, T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(clientSession, fileObject, file, file.getName(), interactingEntity);
	}
	
	public ObjectId add(ClientSession clientSession, T fileObject, FileUploadBody uploadBody, InteractingEntity interactingEntity) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(
			FilenameUtils.removeExtension(fileObject.getFileName()),
			FilenameUtils.getExtension(fileObject.getFileName()),
			"uploads"
		);
		
		FileUtils.copyInputStreamToFile(uploadBody.file, tempFile);
		
		ObjectId id = this.add(clientSession, fileObject, tempFile, uploadBody.fileName, interactingEntity);
		
		if(!tempFile.delete()){
			log.warn("Failed to delete temporary upload file: {}", tempFile);
		}
		
		return id;
	}
	
	public ObjectId add(T fileObject, FileUploadBody uploadBody, InteractingEntity interactingEntity) throws IOException {
		return this.add(null, fileObject, uploadBody, interactingEntity);
	}
	
	public ObjectId add(T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(null, fileObject, file, interactingEntity);
	}
	
	protected ObjectId add(ClientSession clientSession, T fileObject, FileMetadata metadata, InputStream is, InteractingEntity interactingEntity) {
		ObjectId newId = null;
		GridFSBucket bucket = this.getGridFSBucket();
		
		boolean sessionGiven = clientSession == null;
		try (
			ClientSession session = (sessionGiven ? null : this.getNewClientSession(true));
		) {
			if (!sessionGiven) {
				clientSession = session;
			}
			
			newId = this.getFileObjectService().add(clientSession, fileObject, interactingEntity);
			
			GridFSUploadOptions ops = this.getUploadOps(metadata);
			String filename = newId.toHexString() + "." + FilenameUtils.getExtension(metadata.getOrigName());
			
			fileObject.setFileName(filename);
			this.getFileObjectService().update(clientSession, fileObject);
			
			if (clientSession == null) {
				bucket.uploadFromStream(filename, is, ops);
			} else {
				bucket.uploadFromStream(clientSession, filename, is, ops);
			}
			
			if (!sessionGiven) {
				clientSession.commitTransaction();
			}
		}
		
		return newId;
	}
	
	/**
	 * @param clientSession
	 * @param id
	 * @param metadata
	 * @param is
	 * @param interactingEntity
	 *
	 * @return
	 */
	protected int updateFile(ClientSession clientSession, ObjectId id, FileMetadata metadata, InputStream is, InteractingEntity interactingEntity) {
		T object = this.getFileObjectService().get(id);
		GridFSBucket bucket = this.getGridFSBucket();
		
		boolean sessionGiven = clientSession == null;
		try (
			ClientSession session = (sessionGiven ? null : this.getNewClientSession(true));
		) {
			if (!sessionGiven) {
				clientSession = session;
			}
			
			GridFSUploadOptions ops = this.getUploadOps(metadata);
			String filename = object.getFileName();
			
			if (clientSession == null) {
				bucket.uploadFromStream(filename, is, ops);
			} else {
				bucket.uploadFromStream(clientSession, filename, is, ops);
			}
			
			this.getFileObjectService().addHistoryFor(object, interactingEntity, new NewFileVersionEvent());
			
			if (!sessionGiven) {
				clientSession.commitTransaction();
			}
		}
		
		return this.getRevisions(clientSession, id).size() - 1;
	}
	
	public int updateFile(ClientSession clientSession, ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			return this.updateFile(
				clientSession,
				id,
				fileMetadata,
				is,
				interactingEntity
			);
		}
	}
	
	public int updateFile(ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		return this.updateFile(null, id, file, interactingEntity);
	}
}