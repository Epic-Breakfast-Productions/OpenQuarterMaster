package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InventoryItemService extends MongoService<InventoryItem> {

    InventoryItemService(){//required for DI
        super(null,null, null, null, null, null);
    }

    @Inject
    InventoryItemService(
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
            String database
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                InventoryItem.class
        );
    }
}
