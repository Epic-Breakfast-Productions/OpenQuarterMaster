package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.media.FileMetadata;

@Slf4j
public abstract class MongoFileService<T extends MainObject, S extends SearchObject<T>> extends MongoService<T, S> {
	
	GridFSBucket gridFSBucket = null;
	@Getter(AccessLevel.PRIVATE)
	Codec<FileMetadata> fileMetadataCodec;
	
	public MongoFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
	}
	
	protected MongoFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz
	) {
		super(objectMapper, mongoClient, database, clazz);
		this.fileMetadataCodec = this.getDatabase().getCodecRegistry().get(FileMetadata.class);
	}
	
	@Override
	public String getCollectionName() {
		return super.getCollectionName() + "-grid";
	}
	
	protected GridFSBucket getGridFSBucket() {
		if (this.gridFSBucket == null) {
			this.gridFSBucket = GridFSBuckets.create(this.getDatabase(), this.getCollectionName());
		}
		return this.gridFSBucket;
	}
	
	protected Document metadataToDocument(FileMetadata object) {
		BsonDocument outDoc = new BsonDocument();
		BsonWriter writer = new BsonDocumentWriter(outDoc);
		
		this.getFileMetadataCodec().encode(
			writer,
			object,
			EncoderContext.builder().build()
		);
		
		return new Document(outDoc);
	}
	
	protected GridFSUploadOptions getUploadOps(FileMetadata metadata){
		return new GridFSUploadOptions()
				   .chunkSizeBytes(1048576)
				   .metadata(
					   this.metadataToDocument(metadata)
				   );
	}
}
