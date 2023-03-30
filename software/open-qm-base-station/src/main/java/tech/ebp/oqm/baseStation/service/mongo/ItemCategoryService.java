package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.rest.search.CategoriesSearch;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.storage.ItemCategory;
import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.lib.core.rest.tree.ParentedMainObjectTree;
import tech.ebp.oqm.lib.core.rest.tree.itemCategory.ItemCategoryTree;
import tech.ebp.oqm.lib.core.rest.tree.itemCategory.ItemCategoryTreeNode;
import tech.ebp.oqm.lib.core.rest.tree.storageBlock.StorageBlockTree;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collection;

@Slf4j
@ApplicationScoped
public class ItemCategoryService extends HasParentObjService<ItemCategory, CategoriesSearch, ItemCategoryTreeNode> {
	
	ItemCategoryService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ItemCategoryService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ItemCategory.class,
			false
		);
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
}
