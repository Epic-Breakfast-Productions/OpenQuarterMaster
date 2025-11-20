package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.media.file.FileAttachment;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.core.api.model.rest.tree.storageBlock.StorageBlockTree;
import tech.ebp.oqm.core.api.model.rest.tree.storageBlock.StorageBlockTreeNode;
import tech.ebp.oqm.core.api.model.rest.search.StorageBlockSearch;
import tech.ebp.oqm.core.api.exception.db.DbModValidationException;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@Named("StorageBlockService")
@Slf4j
@ApplicationScoped
public class StorageBlockService extends HasParentObjService<StorageBlock, StorageBlockSearch, CollectionStats, StorageBlockTreeNode>{
	
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	ItemCheckoutService itemCheckoutService;
	
	public StorageBlockService() {
		super(StorageBlock.class, false);
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, StorageBlock storageBlock, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, storageBlock, clientSession);
		
		Bson parentFilter = and(
			eq("label", storageBlock.getLabel()),
			eq("location", storageBlock.getLocation()),
			eq("parent", storageBlock.getParent())
		);
		
		//TODO:: remember what this does and why
		if(newObject){
			long count = this.count(oqmDbIdOrName, clientSession, parentFilter);
			if(count > 0){
				throw new DbModValidationException("Already have a storage block with the same label, location, and parent present.");
			}
		} else {
			List<StorageBlock> results = this.list(oqmDbIdOrName, clientSession, parentFilter, null, null);
			
			if(!results.isEmpty()){
				if(results.size() > 1 || !results.get(0).getId().equals(storageBlock.getId())){
					throw new DbModValidationException("Already have a storage block with the same label, location, and parent present.");
				}
			}
		}
		
		//ensure parent exists, not infinite loop
		if (storageBlock.getId() != null && storageBlock.hasParent()) {
			if(storageBlock.getId().equals(storageBlock.getParent())){
				throw new DbModValidationException("Storage block cannot be a parent to itself.");
			}
			
			//exists
			StorageBlock curParent;
			try {
				curParent = this.get(oqmDbIdOrName, clientSession, storageBlock.getParent());
			} catch(DbNotFoundException e){
				throw new DbModValidationException("No parent exists for parent given.", e);
			}
			//no inf loop
			while (curParent.hasParent()){
				if(storageBlock.getId().equals(curParent.getParent())){
					throw new DbModValidationException("Not allowed to make parental loop.");
				}
				curParent = this.get(oqmDbIdOrName, clientSession, curParent.getParent());
			}
			
		}
		
		{//TODO:: check that parent isn't an infinite loop
		
		}
	}
	
	@Override
	protected ParentedMainObjectTree<StorageBlock, StorageBlockTreeNode> getNewTree() {
		return new StorageBlockTree();
	}
	
	/**
	 *
	 * @param clientSession
	 * @param image
	 * @return
	 */
	public Set<ObjectId> getBlocksReferencing(String oqmDbIdOrName, ClientSession clientSession, Image image){
		// { "imageIds": {$elemMatch: {$eq:ObjectId('6335f3c338a79a4377aea064')}} }
		// https://stackoverflow.com/questions/76178393/how-to-recreate-bson-query-with-elemmatch
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			//			elemMatch("imageIds", eq(image.getId())),
			eq("imageIds", image.getId()),
			null,
			null
		).map(StorageBlock::getId).into(list);
		return list;
	}
	
	public Set<ObjectId> getBlocksReferencing(String oqmDbIdOrName, ClientSession clientSession, ItemCategory itemCategory){
		// { "imageIds": {$elemMatch: {$eq:ObjectId('6335f3c338a79a4377aea064')}} }
		// https://stackoverflow.com/questions/76178393/how-to-recreate-bson-query-with-elemmatch
		
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("storedCategories", itemCategory.getId()),
			null,
			null
		).map(StorageBlock::getId).into(list);
		return list;
	}
	
	public Set<ObjectId> getBlocksReferencing(String oqmDbIdOrName, ClientSession clientSession, FileAttachment fileAttachment){
		// https://stackoverflow.com/questions/76178393/how-to-recreate-bson-query-with-elemmatch
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("attachedFiles", fileAttachment.getId()),
			null,
			null
		).map(StorageBlock::getId).into(list);
		return list;
	}
	
	@WithSpan
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(String oqmDbIdOrName, ClientSession cs, StorageBlock storageBlock) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(oqmDbIdOrName, cs, storageBlock);
		
		Set<ObjectId> refs = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			cs,
			eq(
				"parent",
				storageBlock.getId()
			),
			null,
			null
		).map(StorageBlock::getId).into(refs);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.getClazz().getSimpleName(), refs);
		}
		
		refs = this.inventoryItemService.getItemsReferencing(oqmDbIdOrName, cs, storageBlock);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		
		refs = this.itemCheckoutService.getItemCheckoutsReferencing(oqmDbIdOrName, cs, storageBlock);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.itemCheckoutService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return StorageBlock.CUR_SCHEMA_VERSION;
	}
}
