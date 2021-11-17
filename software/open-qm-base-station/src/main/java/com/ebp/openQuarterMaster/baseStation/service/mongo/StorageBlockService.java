package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.storage.StorageBlock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;

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

    public SearchResult<StorageBlock> search(
            String label,
            String location,
            List<String> parents,
            List<String> keywords,
            //TODO:: capacity
            Bson sort,
            PagingOptions pagingOptions
    ) {
        log.info(
                "Searching for items with: label=\"{}\", keywords={}",
                label,
                keywords
        );
        List<Bson> filters = new ArrayList<>();
//        if (name != null && !name.isBlank()) {
//            filters.add(regex("name", SearchUtils.getSearchTermPattern(name)));
//        }
        //TODO:: keywords, storedType

        Bson filter = (filters.isEmpty() ? null : and(filters));

        List<StorageBlock> list = this.list(
                filter,
                sort,
                pagingOptions
        );

        return new SearchResult<>(
                list,
                this.count(filter)
        );
    }


}
