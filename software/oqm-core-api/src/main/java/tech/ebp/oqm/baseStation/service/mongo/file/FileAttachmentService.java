package tech.ebp.oqm.baseStation.service.mongo.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.collectionStats.CollectionStats;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;
import tech.ebp.oqm.baseStation.model.object.media.Image;
import tech.ebp.oqm.baseStation.model.object.media.file.FileAttachment;
import tech.ebp.oqm.baseStation.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.baseStation.rest.file.FileAttachmentUploadBody;
import tech.ebp.oqm.baseStation.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * TODO:: figure out how to do this with gridfs https://www.mongodb.com/docs/drivers/java/sync/v4.3/fundamentals/gridfs/
 */
@Slf4j
@ApplicationScoped
public class FileAttachmentService extends MongoHistoriedFileService<FileAttachment, FileAttachmentSearch, FileAttachmentGet> {
	
	StorageBlockService storageBlockService;
	InventoryItemService inventoryItemService;
	
	FileAttachmentService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	FileAttachmentService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		TempFileService tempFileService,
		StorageBlockService storageBlockService,
		InventoryItemService inventoryItemService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			FileAttachment.class,
			false,
			tempFileService,
			new FileAttachmentObjectService(
				objectMapper,
				mongoClient,
				database
			)
		);
		((FileAttachmentObjectService)this.getFileObjectService()).setFileService(this);
		this.storageBlockService = storageBlockService;
		this.inventoryItemService = inventoryItemService;
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, FileAttachment newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
	}
	
	@Override
	public FileAttachmentGet fileObjToGet(FileAttachment obj) {
		return FileAttachmentGet.fromFileAttachment(obj, this.getRevisions(obj.getId()));
	}
	
	private static class FileAttachmentObjectService extends MongoHistoriedObjectService<FileAttachment, FileAttachmentSearch, CollectionStats> {
		
		@Setter
		@Getter(AccessLevel.PRIVATE)
		private FileAttachmentService fileService;
		
		FileAttachmentObjectService() {//required for DI
			super(null, null, null, null, null, null, false, null);
		}
		
		FileAttachmentObjectService(
			ObjectMapper objectMapper,
			MongoClient mongoClient,
			String database
		) {
			super(
				objectMapper,
				mongoClient,
				database,
				FileAttachment.class,
				false
			);
//			this.fileService = fileService;
			//        this.validator = validator;
		}
		
		@Override
		public CollectionStats getStats() {
			return super.addBaseStats(CollectionStats.builder())
					   .build();
		}
		
		//TODO:: make this work somehow
//		@Override
//		public FindIterable<FileAttachment> listIterator(ClientSession clientSession, Bson filter, Bson sort, PagingOptions pageOptions) {
//			return super.listIterator(clientSession, filter, sort, pageOptions).map(
//				(FileAttachment a)->{
//					return FileAttachmentGet.fromFileAttachment(a, this.getFileService().getRevisions(a.getId()));
//				}
//			);
//		}
	}
	
	@WithSpan
	public int updateFile(ClientSession clientSession, ObjectId fileId, FileAttachmentUploadBody uploadBody, InteractingEntity interactingEntity) throws IOException {
		FileAttachment attachmentObj = this.getObject(fileId);
		attachmentObj.setFileName(uploadBody.fileName);
		attachmentObj.setDescription(uploadBody.description);
		
		return super.updateFile(clientSession, attachmentObj, uploadBody, interactingEntity);
	}
	@WithSpan
	public int updateFile(ObjectId fileId, FileAttachmentUploadBody uploadBody, InteractingEntity interactingEntity) throws IOException {
		return this.updateFile(null, fileId, uploadBody, interactingEntity);
	}
	
	@WithSpan
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(ClientSession cs, FileAttachment objectToRemove) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(cs, objectToRemove);
		
		Set<ObjectId> refs = this.storageBlockService.getBlocksReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		
		refs = this.inventoryItemService.getItemsReferencing(cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
}
