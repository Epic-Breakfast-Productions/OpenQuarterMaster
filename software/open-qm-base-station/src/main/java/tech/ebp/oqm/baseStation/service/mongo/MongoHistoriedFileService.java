package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.lib.core.object.FileMainObject;
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
		boolean allowNullEntityForCreate
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			metadataClazz
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
	
	
	
	
	public ObjectId add(ClientSession clientSession, T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		
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
	
	//	public T getData(ClientSession clientSession, ObjectId id, OutputStream os){
	//		GridFSBucket bucket = this.getGridFSBucket();
	//
	//		this.getGridFSBucket();
	//
	//		this.getGridFSBucket().downloadToStream(id, os);
	//
	////		gridFSBucket.downloadToStream("myProject.zip", streamToDownloadTo, downloadOptions);
	////		streamToDownloadTo.flush();
	//
	//
	//
	//
	//
	//
	//		GridFSUploadOptions ops = new GridFSUploadOptions()
	//									  .chunkSizeBytes(1048576)
	//									  .metadata(this.objectToDocument(attachmentData));
	//
	//		ObjectId newId;
	//
	//		if(clientSession == null) {
	//			newId = bucket.uploadFromStream(attachmentData.getFileName(), is, ops);
	//		} else {
	//			newId = bucket.uploadFromStream(clientSession, attachmentData.getFileName(), is, ops);
	//		}
	//
	//		return newId;
	//	}
	
}
