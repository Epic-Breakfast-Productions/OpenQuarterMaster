package tech.ebp.oqm.core.api.service.mongo.file;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.FileMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.media.FileHashes;
import tech.ebp.oqm.core.api.model.object.media.FileMetadata;
import tech.ebp.oqm.core.api.model.rest.management.CollectionClearResult;
import tech.ebp.oqm.core.api.model.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.TempFileService;
import tech.ebp.oqm.core.api.service.mongo.MongoObjectService;
import tech.ebp.oqm.core.api.service.mongo.MongoDbAwareService;
import tech.ebp.oqm.core.api.exception.db.DbDeleteRelationalException;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.service.mongo.utils.FileContentsGet;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;

/**
 * The main abstract service dealing with files.
 *
 * Files in this system are held in two parts;
 *  - MongoDB's Gridfs; Holds files, metadata, and revisions organized by individual filename
 *  - A separate "Object" collection to provide a common id (used for the Gridfs filename) and attributes
 *
 *
 *
 * @param <T>
 * @param <S>
 * @param <X>
 * @param <G>
 */
@Slf4j
public abstract class MongoFileService<T extends FileMainObject, S extends SearchObject<T>, X extends CollectionStats, G extends MainObject & FileGet> extends MongoDbAwareService<T, S,
																																											  X> {
	
	private Map<ObjectId, GridFSBucket> gridBuckets = new HashMap<>();
	
	@Getter(AccessLevel.PUBLIC)
	Codec<FileMetadata> fileMetadataCodec;
	
	@Inject
	@Getter(AccessLevel.PROTECTED)
	TempFileService tempFileService;
	
	@Getter
	protected String objectName;
	
	//TODO:: map of db's that have had these collections initted
	
	protected MongoFileService(
		Class<T> clazz,
		String objectName
	) {
		super(clazz);
		this.objectName = objectName;
	}
	
	protected void setupBucket(GridFSBucket bucket){
		log.info("Ensuring bucket setup.");
		//TODO:: https://jira.mongodb.org/browse/JAVA-4887  #51 once this is done, cleanup other modifying session logic
		// should probably be a TODO to remove this, but unsure how we ever might be able to.
		//ensure gridfs bucket storage is initialized. Required to avoid trying to create during a transaction, which is unsupported by Mongodb.
		this.getFileObjectService();
		if(bucket.find().limit(1).first() == null){
			log.info("Brand new bucket, setting up");
			FileMetadata metadata = new FileMetadata(
				"disregard_init_file_disregard",
				"txt",
				0,
				FileHashes.builder().md5("").sha1("").sha256("").build(),
				FileMetadata.TIKA.detect("plain.txt"),
				ZonedDateTime.now()
			);
			
			GridFSUploadOptions ops = this.getUploadOps(metadata);
			String filename = "init";
			
			ObjectId id = bucket.uploadFromStream(filename, new ByteArrayInputStream("".getBytes()), ops);
			bucket.delete(id);
		} else {
			log.info("Bucket already existent.");
		}
	}
	
	@Override
	public String getCollectionName() {
		return super.getCollectionName() + "-" + this.getObjectName();
	}
	
	public String getBucketName(){
		return this.getCollectionName() + "-grid";
	}
	
	public GridFSBucket getGridFSBucket(DbCacheEntry db) {
		if(!this.gridBuckets.containsKey(db.getDbId())){
			log.info("Creating new bucket.");
			GridFSBucket newBucket = GridFSBuckets.create(db.getMongoDatabase(), this.getBucketName());
			if(this.fileMetadataCodec == null){
				this.fileMetadataCodec = db.getMongoDatabase().getCodecRegistry().get(FileMetadata.class);
			}
			this.setupBucket(newBucket);
			this.gridBuckets.put(
				db.getDbId(),
				newBucket
			);
		}
		return this.gridBuckets.get(db.getDbId());
	}
	
	public GridFSBucket getGridFSBucket(String oqmDbIdOrName) {
		return this.getGridFSBucket(this.getOqmDatabaseService().getOqmDatabase(oqmDbIdOrName));
	}

	public GridFSBucket getGridFSBucket(ObjectId oqmDbId) {
		return this.getGridFSBucket(oqmDbId.toHexString());
	}
	
	protected FileMetadata documentToMetadata(Document doc) {
		BsonReader reader = new BsonDocumentReader(doc.toBsonDocument());
		
		return this.getFileMetadataCodec().decode(
			reader,
			DecoderContext.builder().build()
		);
	}
	
	public GridFSUploadOptions getUploadOps(FileMetadata metadata) {
		return new GridFSUploadOptions()
				   .chunkSizeBytes(1048576)//TODO:: move to config
				   .metadata(metadata.toDocument(this.getFileMetadataCodec()));
	}
	
	/**
	 * Gets the mongo service responsible for handling the associated data objects
	 * @return
	 */
	public abstract MongoObjectService<T, S, X> getFileObjectService();
	
	public abstract G fileObjToGet(String oqmDbIdOrName, T obj);
	
	public G getObjGet(String dbIdOrName, ObjectId id) {
		return this.fileObjToGet(dbIdOrName, this.getObj(dbIdOrName, id));
	}
	public G getObjGet(String dbIdOrName, String id) {
		return this.fileObjToGet(dbIdOrName, this.getObj(dbIdOrName, id));
	}
	public T getObj(String dbIdOrName, ObjectId id) {
		return this.getFileObjectService().get(dbIdOrName, id);
	}
	public T getObj(String dbIdOrName, String id) {
		return this.getFileObjectService().get(dbIdOrName, id);
	}
	
	public SearchResult<G> search(String dbIdOrName, S search){
		List<Bson> filters = search.getSearchFilters();
		Bson filter = (filters.isEmpty() ? null : and(filters));
		FindIterable<T> searchResult = this.getFileObjectService().listIterator(dbIdOrName, search);
		
		List<G> results = new ArrayList<>();
		searchResult
			.map(g->this.fileObjToGet(dbIdOrName, g))
			.into(results);
		
		return new SearchResult<>(
			results,
			(int) this.getFileObjectService().count(dbIdOrName, filter),
			!filters.isEmpty(),
			search.getPagingOptions(),
			search
		);
	}
	
	public long count(String dbIdOrName, ClientSession clientSession) {
		return this.getFileObjectService().count(dbIdOrName, clientSession);
	}
	
	public long count(String dbIdOrName) {
		return this.count(dbIdOrName, null);
	}
	
	public Iterator<T> objectIterator(String dbIdOrName) {
		return this.getFileObjectService().iterator(dbIdOrName);
	}
	
	public Iterator<GridFSFile> fileIterator(String dbIdOrName) {
		return this.getGridFSBucket(dbIdOrName).find().iterator();
	}
	
	public T getObject(String dbIdOrName, ClientSession clientSession, ObjectId id) {
		return this.getFileObjectService().get(dbIdOrName, clientSession, id);
	}
	
	public T getObject(String dbIdOrName, ObjectId id) {
		return this.getObject(dbIdOrName, null, id);
	}
	
	protected String getFileName(String dbIdOrName, ClientSession clientSession, ObjectId id) {
		return this.getObject(dbIdOrName, clientSession, id).getGridfsFileName();
	}
	
	protected Bson getFileNameQuery(String dbIdOrName, ClientSession clientSession, ObjectId id) {
		return Filters.eq("filename", this.getFileName(dbIdOrName, clientSession, id));
	}
	
	public int getNumRevisions(String dbIdOrName, ClientSession clientSession, ObjectId id) {
		//TODO:: better/ more efficient way?
		Bson query = this.getFileNameQuery(dbIdOrName, clientSession, id);
		
		GridFSFindIterable iterable;
		GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
		if (clientSession == null) {
			iterable = bucket.find(query);
		} else {
			iterable = bucket.find(clientSession, query);
		}
		
		List<GridFSFile> output = new ArrayList<>();
		iterable.into(output);
		return output.size();
	}
	public int getLatestVersionNum(String dbIdOrName, ClientSession clientSession, ObjectId id) {
		return this.getNumRevisions(dbIdOrName, clientSession, id);
	}
	public int getLatestVersionNum(String dbIdOrName, String id) {
		return this.getLatestVersionNum(dbIdOrName, null, new ObjectId(id));
	}
	
	public List<FileMetadata> getRevisions(String dbIdOrName, ClientSession clientSession, ObjectId id) {
		Bson query = this.getFileNameQuery(dbIdOrName, clientSession, id);
		
		GridFSFindIterable iterable;
		GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
		if (clientSession == null) {
			iterable = bucket.find(query);
		} else {
			iterable = bucket.find(clientSession, query);
		}
		iterable.sort(Sorts.ascending("uploadDate"));
		
		List<FileMetadata> output = new ArrayList<>();
		
		iterable.forEach((GridFSFile file)->{
			output.add(this.documentToMetadata(file.getMetadata()));
		});
		
		return output;
	}
	
	public List<FileMetadata> getRevisions(String dbIdOrName, ObjectId id) {
		return this.getRevisions(dbIdOrName, null, id);
	}
	
	public FileMetadata getFileMetadata(String dbIdOrName, ClientSession clientSession, ObjectId id, int revisionNum){
		return this.getRevisions(dbIdOrName, clientSession, id).get(revisionNum - 1);
	}
	public FileMetadata getFileMetadata(String dbIdOrName, String id, int revisionNum){
		return this.getFileMetadata(dbIdOrName, null, new ObjectId(id), revisionNum);
	}
	
	public FileContentsGet getFile(String dbIdOrName, ClientSession clientSession, ObjectId id, int revisionNum) throws IOException {
		int revisionIndex = revisionNum - 1;
		T fileObj = this.getObject(dbIdOrName, clientSession, id);
		
		FileContentsGet.Builder<?, ?> outputBuilder = FileContentsGet.builder();
		
		List<FileMetadata> revisions = this.getRevisions(dbIdOrName, clientSession, id);
		
		FileMetadata metadata = revisions.get(revisionIndex);
		outputBuilder.metadata(metadata);
		
		GridFSDownloadOptions ops = new GridFSDownloadOptions().revision(revisionIndex);
		
		outputBuilder.contents(
			this.downloadGridfsFile(
				dbIdOrName,
				clientSession,
				fileObj.getGridfsFileName(),
				this.getTempFileName(fileObj.getGridfsFileName(), revisionNum),
				ops)
		);
		
		return outputBuilder.build();
	}
	
	public FileContentsGet getFile(String dbIdOrName, String id, int revisionNum) throws IOException {
		return this.getFile(dbIdOrName, null, new ObjectId(id), revisionNum);
	}
	
	protected File downloadGridfsFile(String dbIdOrName, ClientSession clientSession, String filename, String tempFilename, GridFSDownloadOptions options) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(tempFilename, this.getCollectionName());
		
		if (!tempFile.exists()) {
			log.info("File needs added to cache: {}", tempFile);
			try (FileOutputStream os = new FileOutputStream(tempFile)) {
				GridFSBucket bucket = this.getGridFSBucket(dbIdOrName);
				if (clientSession == null) {
					bucket.downloadToStream(filename, os, options);
				} else {
					bucket.downloadToStream(clientSession, filename, os, options);
				}
				
				os.flush();
			} catch(Throwable e) {
				tempFile.delete();
				throw e;
			}
		} else {
			log.info("File already exists in fs cache: {}", tempFile);
		}
		return tempFile;
	}
	
	private String getTempFileName(String fileName, int revision) {
		return FilenameUtils.removeExtension(fileName) + "-" + revision + FilenameUtils.getExtension(fileName);
	}
	
	/**
	 * Gets a file with a specific ObjectId. This id is _not_ the object't id, but the actual id of the revision.
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public void getFileContents(String dbIdOrName, ObjectId id, OutputStream os) throws IOException {
		this.getGridFSBucket(dbIdOrName).downloadToStream(id, os);
	}
	
	/**
	 * Extend this to provide validation of removal objects; checking dependencies, etc.
	 * @param clientSession The client session, null if none
	 * @param objectToRemove The object being removed
	 */
	public Map<String, Set<ObjectId>> getReferencingObjects(String dbIdOrName, ClientSession clientSession, T objectToRemove){
		return new HashMap<>();
	}
	
	/**
	 * Asserts that the given object is not referenced by any other object.
	 * @param clientSession The client session, null if none
	 * @param objectToRemove The object being removed
	 */
	protected void assertNotReferenced(String dbIdOrName, ClientSession clientSession, T objectToRemove){
		Map<String, Set<ObjectId>> objsWithRefs = this.getReferencingObjects(dbIdOrName, clientSession, objectToRemove);
		if(!objsWithRefs.isEmpty()){
			throw new DbDeleteRelationalException(objectToRemove, objsWithRefs);
		}
	}
	
	@Override
	public CollectionClearResult clear(String dbIdOrName, ClientSession session) {
		this.getGridFSBucket(dbIdOrName).find().forEach((GridFSFile file)->{
			this.getGridFSBucket(dbIdOrName).delete(session, file.getId());
		});
		return this.getFileObjectService().clear(dbIdOrName, session);
	}
}
