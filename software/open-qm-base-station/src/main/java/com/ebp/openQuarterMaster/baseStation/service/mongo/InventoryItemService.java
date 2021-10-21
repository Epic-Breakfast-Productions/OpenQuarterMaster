package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.lib.core.InventoryItem;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InventoryItemService extends MongoService<InventoryItem> {

    InventoryItemService(){//required for DI
        super(null,null, null, null, null);
    }

    @Inject
    InventoryItemService(
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
            String database
    ) {
        super(
                mongoClient,
                database,
                InventoryItem.class
        );
    }
}
