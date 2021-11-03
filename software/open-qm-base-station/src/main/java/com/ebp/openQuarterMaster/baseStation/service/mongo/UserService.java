package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.data.pojos.User;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static com.mongodb.client.model.Filters.eq;

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

    public User getFromEmail(String email) {
        return this.getCollection().find(eq("email", email)).limit(1).first();
    }

    public User getFromLoginRequest(UserLoginRequest loginRequest) {
        return this.getFromEmail(loginRequest.getEmail());
    }
}
