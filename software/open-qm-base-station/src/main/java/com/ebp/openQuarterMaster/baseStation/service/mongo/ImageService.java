package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchUtils;
import com.ebp.openQuarterMaster.lib.core.media.Image;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.and;

@Slf4j
@ApplicationScoped
public class ImageService extends MongoService<Image> {

//    private Validator validator;

    ImageService() {//required for DI
        super(null, null, null, null, null, false, null);
    }

    @Inject
    ImageService(
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
                Image.class,
                false
        );
//        this.validator = validator;
    }

    public SearchResult<Image> search(
            String title,
            List<String> keywords,
            Map<String, String> attributes,
            Bson sort,
            PagingOptions pagingOptions
    ) {
        log.info(
                "Searching for items with: title=\"{}\", keywords={}",
                title,
                keywords
        );
        List<Bson> filters = new ArrayList<>();

        SearchUtils.addBasicSearchFilter(filters, "title", title);
        SearchUtils.addKeywordSearchFilter(filters, keywords);

        //TODO::

        Bson filter = (filters.isEmpty() ? null : and(filters));

        List<Image> list = this.list(
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
