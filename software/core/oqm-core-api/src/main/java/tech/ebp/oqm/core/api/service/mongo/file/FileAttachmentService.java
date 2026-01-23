package tech.ebp.oqm.core.api.service.mongo.file;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.rest.media.file.FileAttachmentGet;
import tech.ebp.oqm.core.api.model.rest.media.file.FileUploadBody;
import tech.ebp.oqm.core.api.model.rest.search.FileAttachmentSearch;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;

import java.util.Map;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class FileAttachmentService extends MongoHistoriedFileService<FileAttachment, FileUploadBody, FileAttachmentSearch, FileAttachmentGet> {
	
	StorageBlockService storageBlockService;
	InventoryItemService inventoryItemService;
	
	public FileAttachmentService() {
		super(FileAttachment.class, "fileAttachment", false);
	}
	
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, FileAttachment newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
	}
	
	@Override
	public FileAttachmentGet fileObjToGet(String oqmDbIdOrName, FileAttachment obj) {
		return FileAttachmentGet.fromFileAttachment(obj, this.getRevisions(oqmDbIdOrName, obj.getId()));
	}
	
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(String oqmDbIdOrName, ClientSession cs, FileAttachment objectToRemove) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(oqmDbIdOrName, cs, objectToRemove);
		
		Set<ObjectId> refs = this.storageBlockService.getBlocksReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		
		refs = this.inventoryItemService.getItemsReferencing(oqmDbIdOrName, cs, objectToRemove);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return FileAttachment.CUR_SCHEMA_VERSION;
	}
}
