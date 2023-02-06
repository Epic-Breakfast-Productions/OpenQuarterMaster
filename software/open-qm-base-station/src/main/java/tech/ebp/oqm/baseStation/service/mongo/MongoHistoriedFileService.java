package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.lib.core.object.FileMainObject;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

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
	private MongoHistoryService<T> historyService = null;
	
	public MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		boolean allowNullEntityForCreate,
		MongoHistoryService<T> historyService,
		CodecRegistry codecRegistry
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection, codecRegistry);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.historyService = historyService;
	}
	
	protected MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz,
		boolean allowNullEntityForCreate,
		CodecRegistry codecRegistry
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			clazz,
			codecRegistry
		);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.historyService = new MongoHistoryService<>(
			objectMapper,
			mongoClient,
			database,
			clazz
		);
	}
	
	
	protected Document objectToDocument(T object){
		BsonDocument outDoc = new BsonDocument();
		BsonWriter writer = new BsonDocumentWriter(outDoc);
		
		this.getCodecRegistry().get(this.getClazz()).encode(
			writer,
			object,
			EncoderContext.builder().build()
		);
		
		return new Document(outDoc);
	}
	
	
	public ObjectId add(ClientSession clientSession, T attachmentData, InputStream is, InteractingEntity interactingEntity){
		GridFSBucket bucket = this.getGridFSBucket();
		
		GridFSUploadOptions ops = new GridFSUploadOptions()
									  .chunkSizeBytes(1048576)
									  .metadata(this.objectToDocument(attachmentData));
		
		ObjectId newId;
		
		if(clientSession == null) {
			newId = bucket.uploadFromStream(attachmentData.getFileName(), is, ops);
		} else {
			newId = bucket.uploadFromStream(clientSession, attachmentData.getFileName(), is, ops);
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
