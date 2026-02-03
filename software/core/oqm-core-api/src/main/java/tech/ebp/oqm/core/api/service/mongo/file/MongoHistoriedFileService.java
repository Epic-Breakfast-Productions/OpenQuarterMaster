package tech.ebp.oqm.core.api.service.mongo.file;

import com.mongodb.client.ClientSession;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.service.mongo.media.FileObjectService;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.FileMainObject;
import tech.ebp.oqm.core.api.model.object.history.events.file.NewFileVersionEvent;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.FileSearchObject;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;

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
public abstract class MongoHistoriedFileService<T extends FileMainObject, U extends FileUploadBody, S extends FileSearchObject<T>, G extends MainObject & FileGet>
	extends MongoFileService<T, S, CollectionStats, G> {
	
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
		Class<T> clazz,
		String objectName,
		boolean allowNullEntityForCreate
	) {
		super(clazz, objectName);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
	}
	
	@PostConstruct
	public void setup() {
		this.fileObjectService = new FileObjectService<>(
			this.getObjectMapper(),
			this.getMongoClient(),
			this.getDatabasePrefix(),
			this.getOqmDatabaseService(),
			this.getCollectionName() + "-obj",
			this.getClazz(),
			this.isAllowNullEntityForCreate()
		);
		this.fileObjectService.setup();
	}
	
	@Override
	public CollectionStats getStats(String dbIdOrName) {
		//TODO:: this should be checked
		return this.getFileObjectService().getStats(dbIdOrName);
	}
	
	public void assertValidMimeType(FileMetadata fileMetadata) {
		if (!this.getAllowedMimeTypes().isEmpty() && !this.getAllowedMimeTypes().contains(fileMetadata.getMimeType())) {
			throw new IllegalArgumentException("File with type not allowed given: " + fileMetadata.getMimeType());
		}
	}
	
	public T add(String dbIdOrName, ClientSession clientSession, T fileObject, File file, String fileName, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		fileMetadata.setOrigName(FilenameUtils.getName(fileName));
		fileObject.setFileName(fileName);
		
		this.assertValidMimeType(fileMetadata);
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
			
			boolean sessionGiven = clientSession != null;
			try (
				ClientSession session = (sessionGiven ? null : this.getNewClientSession(true));
			) {
				if (!sessionGiven) {
					clientSession = session;
				}
				
				this.getFileObjectService().add(dbIdOrName, clientSession, fileObject, interactingEntity);
				
				GridFSUploadOptions ops = this.getUploadOps(fileMetadata);
				
				this.getFileObjectService().update(dbIdOrName, clientSession, fileObject, false);
				bucket.uploadFromStream(clientSession, fileObject.getGridfsFileName(), is, ops);
				
				if (!sessionGiven) {
					clientSession.commitTransaction();
				}
			}
			
			return fileObject;
		}
	}
	
	public T add(String dbIdOrName, ClientSession clientSession, T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(dbIdOrName, clientSession, fileObject, file, file.getName(), interactingEntity);
	}
	
	public T add(String dbIdOrName, ClientSession clientSession, T fileObject, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(
			FilenameUtils.removeExtension(uploadBody.fileName),
			FilenameUtils.getExtension(uploadBody.fileName),
			"uploads"
		);
		
		FileUtils.copyInputStreamToFile(uploadBody.file, tempFile);
		
		T newObj = this.add(dbIdOrName, clientSession, fileObject, tempFile, uploadBody.fileName, interactingEntity);
		
		if (!tempFile.delete()) {
			log.warn("Failed to delete temporary upload file: {}", tempFile);
		}
		
		return newObj;
	}
	
	public T add(String dbIdOrName, T fileObject, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		return this.add(dbIdOrName, null, fileObject, uploadBody, interactingEntity);
	}
	
	public T add(String dbIdOrName, T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(dbIdOrName, null, fileObject, file, interactingEntity);
	}
	
	public int updateFile(String dbIdOrName, ClientSession clientSession, ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			T object = this.getFileObjectService().get(dbIdOrName, id);
			object.setFileName(fileMetadata.getOrigName());
			GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
			
			GridFSUploadOptions ops = this.getUploadOps(fileMetadata);
			String filename = object.getGridfsFileName();
			
			//TODO:: improve flow of handling client session
			//TODO:: use new update obj for file updates
			boolean sessionGiven = clientSession != null;
			if (sessionGiven) {
				bucket.uploadFromStream(clientSession, filename, is, ops);
				this.getFileObjectService().addHistoryFor(dbIdOrName, clientSession, object, interactingEntity, new NewFileVersionEvent());
				return this.getRevisions(dbIdOrName, clientSession, id).size() - 1;
			} else {
				try (
					ClientSession ourSession = this.getNewClientSession(true);
				) {
					bucket.uploadFromStream(ourSession, filename, is, ops);
					this.getFileObjectService().addHistoryFor(dbIdOrName, ourSession, object, interactingEntity, new NewFileVersionEvent());
					ourSession.commitTransaction();
					return this.getRevisions(dbIdOrName, ourSession, id).size() - 1;
				}
			}
		}
	}
	
	public int updateFile(String dbIdOrName, ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		return this.updateFile(dbIdOrName, null, id, file, interactingEntity);
	}
	
	public int updateFile(String dbIdOrName, ClientSession clientSession, ObjectId id, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(
			FilenameUtils.removeExtension(uploadBody.fileName),
			FilenameUtils.getExtension(uploadBody.fileName),
			"uploads"
		);
		FileUtils.copyInputStreamToFile(uploadBody.file, tempFile);
		
		int output = this.updateFile(dbIdOrName, clientSession, id, tempFile, interactingEntity);
		
		if (!tempFile.delete()) {
			log.warn("Failed to delete temporary upload file: {}", tempFile);
		}
		
		return output;
	}
	
	public int updateFile(String dbIdOrName, ClientSession clientSession, String id, U uploadBody, InteractingEntity interactingEntity) throws IOException {
		return this.updateFile(dbIdOrName, clientSession, new ObjectId(id), uploadBody, interactingEntity);
	}
	
	public long removeAll(String dbIdOrName, ClientSession clientSession, InteractingEntity entity) {
		AtomicLong numRemoved = new AtomicLong();
		boolean sessionGiven = clientSession != null;
		if (sessionGiven) {
			this.getFileObjectService().removeAll(dbIdOrName, clientSession, entity);
			GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
			bucket.find(clientSession).forEach((GridFSFile curFile)->{
				bucket.delete(clientSession, curFile.getId());
			});
			this.getFileObjectService().removeAll(dbIdOrName, clientSession, entity);
		} else {
			try (
				ClientSession innerSession = this.getNewClientSession(true)
			) {
				this.getFileObjectService().removeAll(dbIdOrName, innerSession, entity);
				GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
				bucket.find(innerSession).forEach((GridFSFile curFile)->{
					bucket.delete(innerSession, curFile.getId());
				});
				this.getFileObjectService().removeAll(dbIdOrName, innerSession, entity);
				innerSession.commitTransaction();
			}
		}
		
		return numRemoved.get();
	}
	
	public G removeFile(String dbIdOrName, ClientSession cs, ObjectId objectId, InteractingEntity entity) {
		//TODO:: this with cs
		T toRemove = this.getFileObjectService().get(dbIdOrName, cs, objectId);
		G output = this.getObjGet(dbIdOrName, objectId);
		
		this.assertNotReferenced(dbIdOrName, cs, (T) toRemove);
		GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
		
		//TODO:: ensure noting is referencing the file
		if (cs == null) {
			try (ClientSession clientSession = this.getNewClientSession(true)) {
				bucket.find(clientSession, Filters.eq("filename", toRemove.getGridfsFileName())).forEach(
					(GridFSFile file)->{
						bucket.delete(clientSession, file.getId());
					}
				);
				this.getFileObjectService().remove(dbIdOrName, clientSession, toRemove.getId(), entity);
				clientSession.commitTransaction();
			}
		} else {
			bucket.find(cs, Filters.eq("filename", toRemove.getGridfsFileName())).forEach(
				(GridFSFile file)->{
					bucket.delete(cs, file.getId());
				}
			);
			this.getFileObjectService().remove(dbIdOrName, cs, toRemove.getId(), entity);
		}
		
		return output;
	}
}
