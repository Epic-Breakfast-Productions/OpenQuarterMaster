package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TestMongoServiceAllowNullUserCreate extends MongoService<TestMainObject> {

    TestMongoServiceAllowNullUserCreate() {//required for DI
        super(null, null, null, null, null, true, null);
    }

    @Inject
    TestMongoServiceAllowNullUserCreate(
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
                    String database
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                TestMainObject.class,
                true
        );
    }

    TestMongoServiceAllowNullUserCreate(
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            String database,
            boolean allowNullUserForCreate
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                TestMainObject.class,
                allowNullUserForCreate
        );
    }
}
