package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.data.pojos.User;
import com.ebp.openQuarterMaster.baseStation.data.pojos.UserLoginRequest;
import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Set;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class UserService extends MongoService<User> {

    private Validator validator;
    private AuthMode authMode;

    UserService() {//required for DI
        super(null, null, null, null, null, null);
    }

    @Inject
    UserService(
            Validator validator,
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
                    String database,
            @ConfigProperty(name = "service.authMode")
                    AuthMode authMode
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                User.class
        );
        this.validator = validator;
        this.authMode = authMode;
    }

    public User getFromEmail(String email) {
        return this.getCollection().find(eq("email", email)).limit(1).first();
    }

    public User getFromLoginRequest(UserLoginRequest loginRequest) {
        return this.getFromEmail(loginRequest.getEmail());
    }

    private User getExternalUser(String externalSource, String externalId) {
        return this.getCollection().find(eq("externIds." + externalSource, externalId)).limit(1).first();
    }

    private User getOrCreateExternalUser(JsonWebToken jwt) {
        String externalSource = jwt.getIssuer();
        String externalId = jwt.getClaim(JwtService.JWT_USER_ID_CLAIM);
        User user = this.getExternalUser(externalSource, externalId);

        if (user != null) {
            //TODO:: update from given jwt, if needed
            return user;
        }

        User.Builder userBuilder = User.builder(jwt);

        userBuilder.externIds(new HashMap<>() {{
            put(externalSource, externalId);
        }});

        user = userBuilder.build();

        Set<ConstraintViolation<User>> validationErrs = this.validator.validate(user);
        if (!validationErrs.isEmpty()) {
            throw new IllegalStateException("Resulting user from jwt wasn't valid.");
        }

        this.add(user);
        return user;
    }

    public User getFromJwt(JsonWebToken jwt) {
        String userId = jwt.getClaim(JwtService.JWT_USER_ID_CLAIM);

        switch (this.authMode) {
            case SELF:
                return this.get(userId);
            case EXTERNAL:
                return this.getOrCreateExternalUser(jwt);
        }
        return null;
    }
}
