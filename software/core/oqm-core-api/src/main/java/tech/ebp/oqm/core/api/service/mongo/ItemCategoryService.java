package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.core.api.model.rest.tree.itemCategory.ItemCategoryTree;
import tech.ebp.oqm.core.api.model.rest.tree.itemCategory.ItemCategoryTreeNode;
import tech.ebp.oqm.core.api.model.rest.search.ItemCategorySearch;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.mongodb.client.model.Filters.eq;

@Named("ItemCategoryService")
@Slf4j
@ApplicationScoped
public class ItemCategoryService extends HasParentObjService<ItemCategory, ItemCategorySearch, CollectionStats, ItemCategoryTreeNode> {
	
	@Inject
	InventoryItemService inventoryItemService;
	@Inject
	StorageBlockService storageBlockService;
	
	public ItemCategoryService() {
		super(ItemCategory.class, false);
	}
	
	@Override
	public CollectionStats getStats(String oqmDbIdOrName) {
		return super.addBaseStats(oqmDbIdOrName, CollectionStats.builder())
				   .build();
	}
	
	@Override
	public void ensureObjectValid(String oqmDbIdOrName, boolean newObject, ItemCategory newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(oqmDbIdOrName, newObject, newOrChangedObject, clientSession);
		//TODO:: this
	}
	
	@Override
	protected ParentedMainObjectTree<ItemCategory, ItemCategoryTreeNode> getNewTree() {
		return new ItemCategoryTree();
	}
	
	public Set<ObjectId> getItemCatsReferencing(String oqmDbIdOrName, ClientSession clientSession, Image image) {
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			oqmDbIdOrName,
			clientSession,
			eq("imageIds", image.getId()),
			null,
			null
		).map(ItemCategory::getId).into(list);
		return list;
	}
	
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(String oqmDbIdOrName, ClientSession cs, ItemCategory itemCategory) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(oqmDbIdOrName, cs, itemCategory);
		
		Set<ObjectId> refs = new TreeSet<>();
		
		this.listIterator(
			oqmDbIdOrName,
			cs,
			eq(
				"parent",
				itemCategory.getId()
			),
			null,
			null
		).map(ItemCategory::getId).into(refs);
		if (!refs.isEmpty()) {
			objsWithRefs.put(this.getClazz().getSimpleName(), refs);
		}
		
		refs = this.storageBlockService.getBlocksReferencing(oqmDbIdOrName, cs, itemCategory);
		if (!refs.isEmpty()) {
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		refs = this.inventoryItemService.getItemsReferencing(oqmDbIdOrName, cs, itemCategory);
		if (!refs.isEmpty()) {
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
	
	@Override
	public int getCurrentSchemaVersion() {
		return ItemCategory.CUR_SCHEMA_VERSION;
	}
}
