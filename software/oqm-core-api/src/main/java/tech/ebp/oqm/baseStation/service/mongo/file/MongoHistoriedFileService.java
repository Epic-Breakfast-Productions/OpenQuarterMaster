package tech.ebp.oqm.baseStation.service.mongo.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.service.mongo.media.FileObjectService;
import tech.ebp.oqm.baseStation.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.file.NewFileVersionEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;
import tech.ebp.oqm.baseStation.rest.search.FileSearchObject;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class MongoHistoriedFileService<T extends FileMainObject, U extends FileUploadBody, S extends FileSearchObject<T>, G extends FileGet> extends MongoFileService<T
																																												   , S,
																																								  CollectionStats,
																																											   G> {
	
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
	private MongoHistoriedObjectService<T, S, CollectionStats> fileObjectService = null;
	
	@Getter
	protected Set<String> allowedMimeTypes = new HashSet<>();
	
	public MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		boolean allowNullEntityForCreate,
		MongoHistoriedObjectService<T, S, CollectionStats> fileObjectService
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.fileObjectService = fileObjectService;
	}
	
	protected MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> metadataClazz,
		boolean allowNullEntityForCreate,
		TempFileService tempFileService,
		String fileCollName
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
			new FileObjectService<>(
				objectMapper,
				mongoClient,
				database,
				metadataClazz,
				fileCollName
			);
	}
	
	@Override
	public CollectionStats getStats() {
		//TODO:: this should be checked
		return this.getFileObjectService().getStats();
	}
	
	public void assertValidMimeType(FileMetadata fileMetadata){
		if(!this.getAllowedMimeTypes().isEmpty() && !this.getAllowedMimeTypes().contains(fileMetadata.getMimeType())){
			throw new IllegalArgumentException("File with type not allowed given: " + fileMetadata.getMimeType());
		}
	}
	
	@WithSpan
	public ObjectId add(ClientSession clientSession, T fileObject, File file, String fileName, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		fileMetadata.setOrigName(FilenameUtils.getName(fileName));
		fileObject.setFilename(fileMetadata.getOrigName());
		
		this.assertValidMimeType(fileMetadata);
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			ObjectId newId = null;
			GridFSBucket bucket = this.getGridFSBucket();
			
			boolean sessionGiven = clientSession != null;
			try (
				ClientSession session = (sessionGiven ? null : this.getNewClientSession(true));
			) {
				if (!sessionGiven) {
					clientSession = session;
				}
				
				newId = this.getFileObjectService().add(clientSession, fileObject, interactingEntity);
				
				GridFSUploadOptions ops = this.getUploadOps(fileMetadata);
				
				this.getFileObjectService().update(clientSession, fileObject);
				bucket.uploadFromStream(clientSession, fileObject.getGridfsFileName(), is, ops);
				
				if (!sessionGiven) {
					clientSession.commitTransaction();
				}
			}
			
			return newId;
		}
	}
	
	public ObjectId add(ClientSession clientSession, T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(clientSession, fileObject, file, file.getName(), interactingEntity);
	}
	
	@WithSpan
	public ObjectId add(ClientSession clientSession, T fileObject, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(
			FilenameUtils.removeExtension(uploadBody.fileName),
			FilenameUtils.getExtension(uploadBody.fileName),
			"uploads"
		);
		
		FileUtils.copyInputStreamToFile(uploadBody.file, tempFile);
		
		ObjectId id = this.add(clientSession, fileObject, tempFile, uploadBody.fileName, interactingEntity);
		
		if (!tempFile.delete()) {
			log.warn("Failed to delete temporary upload file: {}", tempFile);
		}
		
		return id;
	}
	
	public ObjectId add(T fileObject, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		return this.add(null, fileObject, uploadBody, interactingEntity);
	}
	
	public ObjectId add(T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(null, fileObject, file, interactingEntity);
	}
	
	@WithSpan
	public int updateFile(ClientSession clientSession, ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			T object = this.getFileObjectService().get(id);
			object.setFilename(fileMetadata.getOrigName());
			GridFSBucket bucket = this.getGridFSBucket();
			
			GridFSUploadOptions ops = this.getUploadOps(fileMetadata);
			String filename = object.getGridfsFileName();
			
			//TODO:: improve flow of handling client session
			//TODO:: use new update obj for file updates
			boolean sessionGiven = clientSession != null;
			if (sessionGiven) {
				bucket.uploadFromStream(clientSession, filename, is, ops);
				this.getFileObjectService().addHistoryFor(clientSession, object, interactingEntity, new NewFileVersionEvent());
				return this.getRevisions(clientSession, id).size() - 1;
			} else {
				try (
					ClientSession ourSession = this.getNewClientSession(true);
				) {
					bucket.uploadFromStream(ourSession, filename, is, ops);
					this.getFileObjectService().addHistoryFor(ourSession, object, interactingEntity, new NewFileVersionEvent());
					ourSession.commitTransaction();
					return this.getRevisions(ourSession, id).size() - 1;
				}
			}
		}
	}
	
	public int updateFile(ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		return this.updateFile(null, id, file, interactingEntity);
	}
	
	@WithSpan
	public int updateFile(ClientSession clientSession, ObjectId id, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(
			FilenameUtils.removeExtension(uploadBody.fileName),
			FilenameUtils.getExtension(uploadBody.fileName),
			"uploads"
		);
		FileUtils.copyInputStreamToFile(uploadBody.file, tempFile);
		
		int output = this.updateFile(clientSession, id, tempFile, interactingEntity);
		
		if (!tempFile.delete()) {
			log.warn("Failed to delete temporary upload file: {}", tempFile);
		}
		
		return output;
	}
	
	public int updateFile(ClientSession clientSession, String id, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		return this.updateFile(clientSession, new ObjectId(id), uploadBody, interactingEntity);
	}
		
		@WithSpan
	public long removeAll(ClientSession clientSession, InteractingEntity entity) {
		AtomicLong numRemoved = new AtomicLong();
		boolean sessionGiven = clientSession != null;
		if (sessionGiven) {
			this.getFileObjectService().removeAll(clientSession, entity);
			GridFSBucket bucket = this.getGridFSBucket();
			bucket.find(clientSession).forEach((GridFSFile curFile)->{
				bucket.delete(clientSession, curFile.getId());
			});
			this.getFileObjectService().removeAll(clientSession, entity);
		} else {
			try (
				ClientSession innerSession = this.getNewClientSession(true)
			) {
				this.getFileObjectService().removeAll(innerSession, entity);
				GridFSBucket bucket = this.getGridFSBucket();
				bucket.find(innerSession).forEach((GridFSFile curFile)->{
					bucket.delete(innerSession, curFile.getId());
				});
				this.getFileObjectService().removeAll(innerSession, entity);
				innerSession.commitTransaction();
			}
		}
		
		return numRemoved.get();
	}
	
	public G removeFile(ClientSession cs, ObjectId objectId, InteractingEntity entity){
		//TODO:: this with cs
		T toRemove = this.getFileObjectService().get(cs, objectId);
		G output = this.getObjGet(objectId);
		
		this.assertNotReferenced(cs, (T) toRemove);
		GridFSBucket bucket = this.getGridFSBucket();
		
		//TODO:: ensure noting is referencing the file
		if(cs == null){
			try(ClientSession clientSession = this.getNewClientSession(true)){
				bucket.find(clientSession, Filters.eq("filename", toRemove.getGridfsFileName())).forEach(
					(GridFSFile file)->{
						bucket.delete(clientSession, file.getId());
					}
				);
				this.getFileObjectService().remove(clientSession, toRemove.getId(), entity);
				clientSession.commitTransaction();
			}
		}else {
			bucket.find(cs, Filters.eq("filename", toRemove.getGridfsFileName())).forEach(
				(GridFSFile file)->{
					bucket.delete(cs, file.getId());
				}
			);
			this.getFileObjectService().remove(cs, toRemove.getId(), entity);
		}
		
		return output;
	}
}
