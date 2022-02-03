package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.tree.StorageBlockTree;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.measure.Quantity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@ApplicationScoped
public class StorageBlockService extends MongoService<StorageBlock> {

    StorageBlockService() {//required for DI
        super(null, null, null, null, null, false, null);
    }

    @Inject
    StorageBlockService(
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
                    String database
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                StorageBlock.class,
                false
        );
    }

    /**
     * Searches for the
     * @param label
     * @param location
     * @param parentLabels
     * @param keywords
     * @param stores
     * @param sort
     * @param pagingOptions
     * @return
     */
    public SearchResult<StorageBlock> search(
            String label,
            String location,
            List<String> parentLabels,
            List<Quantity<?>> capacities,
            List<ObjectId> stores,
            List<String> keywords,
            Map<String, String> attributes,
            Bson sort,
            PagingOptions pagingOptions
    ) {
        log.info(
                "Searching for storage blocks with: label=\"{}\", keywords={}",
                label,
                keywords
        );
        List<Bson> filters = new ArrayList<>();

        SearchUtils.addBasicSearchFilter(filters, "label", label);
        SearchUtils.addBasicSearchFilter(filters, "location", location);
        SearchUtils.addKeywordSearchFilter(filters, keywords);
        SearchUtils.addAttributeSearchFilters(filters, attributes);

        if(parentLabels != null){
            for(String curParentLabel : parentLabels){
                //TODO::parent labels
            }
        }

        if(capacities!= null) {
            for(Quantity<?> curCap : capacities) {
                //TODO:: capacities with greater than or equal capacity to what was given
            }
        }

        return this.searchResult(filters, sort, pagingOptions);
    }

    public StorageBlockTree getStorageBlockTree(Collection<ObjectId> onlyInclude){
        StorageBlockTree output = new StorageBlockTree();


        FindIterable<StorageBlock> results = getCollection().find();
        output.add(results.iterator());

        if(!onlyInclude.isEmpty()){
            output.cleanupStorageBlockTreeNode(onlyInclude);
        }

        return output;
    }

}
