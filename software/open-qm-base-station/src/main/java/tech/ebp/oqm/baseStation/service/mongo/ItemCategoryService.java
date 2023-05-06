package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.CategoriesSearch;
import tech.ebp.oqm.lib.core.object.media.Image;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.lib.core.rest.tree.itemCategory.ItemCategoryTree;
import tech.ebp.oqm.lib.core.rest.tree.itemCategory.ItemCategoryTreeNode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

@Named("ItemCategoryService")
@Slf4j
@ApplicationScoped
public class ItemCategoryService extends HasParentObjService<ItemCategory, CategoriesSearch, ItemCategoryTreeNode> {
	
	private InventoryItemService inventoryItemService;
	private StorageBlockService storageBlockService;
	
	ItemCategoryService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ItemCategoryService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database,
		InventoryItemService inventoryItemService,
		StorageBlockService storageBlockService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ItemCategory.class,
			false
		);
		this.inventoryItemService = inventoryItemService;
		this.storageBlockService = storageBlockService;
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
