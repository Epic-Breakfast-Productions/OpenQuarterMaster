package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.media.Image;
import tech.ebp.oqm.core.api.model.object.storage.ItemCategory;
import tech.ebp.oqm.core.api.model.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.core.api.model.rest.tree.itemCategory.ItemCategoryTree;
import tech.ebp.oqm.core.api.model.rest.tree.itemCategory.ItemCategoryTreeNode;
import tech.ebp.oqm.core.api.rest.search.ItemCategorySearch;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

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
	
	public ItemCategoryService(){
		this(null);
	}
	
	@Inject
	ItemCategoryService(
		HistoryEventNotificationService hens
	) {
		super(
			ItemCategory.class,
			false,
			hens
		);
	}
	
	@Override
	public CollectionStats getStats() {
		return super.addBaseStats(CollectionStats.builder())
				   .build();
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, ItemCategory newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: this
	}
	
	@Override
	protected ParentedMainObjectTree<ItemCategory, ItemCategoryTreeNode> getNewTree() {
		return new ItemCategoryTree();
	}
	
	public Set<ObjectId> getItemCatsReferencing(ClientSession clientSession, Image image){
		Set<ObjectId> list = new TreeSet<>();
		this.listIterator(
			clientSession,
			eq("imageIds", image.getId()),
			null,
			null
		).map(ItemCategory::getId).into(list);
		return list;
	}
	
	@WithSpan
	@Override
	public Map<String, Set<ObjectId>> getReferencingObjects(ClientSession cs, ItemCategory itemCategory) {
		Map<String, Set<ObjectId>> objsWithRefs = super.getReferencingObjects(cs, itemCategory);
		
		Set<ObjectId> refs = new TreeSet<>();
		
		this.listIterator(
			cs,
			eq(
				"parent",
				itemCategory.getId()
			),
			null,
			null
		).map(ItemCategory::getId).into(refs);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.getClazz().getSimpleName(), refs);
		}
		
		refs = this.storageBlockService.getBlocksReferencing(cs, itemCategory);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.storageBlockService.getClazz().getSimpleName(), refs);
		}
		refs = this.inventoryItemService.getItemsReferencing(cs, itemCategory);
		if(!refs.isEmpty()){
			objsWithRefs.put(this.inventoryItemService.getClazz().getSimpleName(), refs);
		}
		
		return objsWithRefs;
	}
}
