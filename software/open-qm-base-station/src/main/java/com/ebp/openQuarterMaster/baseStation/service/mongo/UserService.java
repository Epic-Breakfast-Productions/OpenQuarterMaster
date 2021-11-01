package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.data.pojos.User;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class UserService extends MongoService<User> {

    UserService(){//required for DI
        super(null,null, null, null, null, null);
    }

    @Inject
    UserService(
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
            String database
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                User.class
        );
    }
}
