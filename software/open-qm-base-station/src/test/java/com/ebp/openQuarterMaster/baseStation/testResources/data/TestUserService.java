package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.github.javafaker.Faker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.EXTERNAL;
import static com.ebp.openQuarterMaster.baseStation.utils.AuthMode.SELF;

@ApplicationScoped
@Slf4j
public class TestUserService {
    public static final String TEST_PASSWORD_ATT_KEY = "TEST_PASSWORD";
    public static final String TEST_EXTERN_ID_ATT_KEY = "TEST_EXTERNAL_KEY";

    @Getter
    @Inject
    UserService userService;

    @Inject
    PasswordService passwordService;

    @ConfigProperty(name = "service.authMode")
    AuthMode authMode;

    @ConfigProperty(name = "test.keycloak.url", defaultValue = "")
    String keycloakUrl;

    @ConfigProperty(name = "test.keycloak.adminName", defaultValue = "admin")
    String keycloakAdminName;
    @ConfigProperty(name = "test.keycloak.adminPass", defaultValue = "admin")
    String keycloakAdminPass;
    @ConfigProperty(name = "service.externalAuth.realm", defaultValue = "")
    String keycloakRealm;
    @ConfigProperty(name = "service.externalAuth.clientId", defaultValue = "")
    String keycloakClientId;
    @ConfigProperty(name = "service.externalAuth.clientSecret", defaultValue = "")
    String keycloakClientSecret;

    Faker faker = Faker.instance();

    private UserRepresentation userToRepresentation(User testUser) {
        UserRepresentation rep = new UserRepresentation();

        rep.setEnabled(true);
        rep.setUsername(testUser.getUsername());
        rep.setFirstName(testUser.getFirstName());
        rep.setLastName(testUser.getLastName());
        rep.setEmail(testUser.getEmail());
        rep.setAttributes(Map.of("origin", List.of("tests")));

        {
            CredentialRepresentation passwordCred = new CredentialRepresentation();
            passwordCred.setTemporary(false);
            passwordCred.setType(CredentialRepresentation.PASSWORD);
            passwordCred.setValue(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));

            //rep.setCredentials(List.of(passwordCred));
        }
        {
            rep.setGroups(testUser.getRoles());
        }

        return rep;
    }

    public void persistTestUser(User testUser) {
        if (SELF.equals(this.authMode)) {
            this.userService.add(testUser, null);
        } else if (EXTERNAL.equals(this.authMode)) {
            try (
                    Keycloak keycloak = KeycloakBuilder.builder()
                            .serverUrl(this.keycloakUrl)
                            .realm("master")
                            .grantType(OAuth2Constants.PASSWORD)
                            .clientId("admin-cli")
                            .username(this.keycloakAdminName)
                            .password(this.keycloakAdminPass)
                            .build();
            ) {

                UserRepresentation userRep = this.userToRepresentation(testUser);

                RealmResource realmResource = keycloak.realm(this.keycloakRealm);
                UsersResource usersResource = realmResource.users();

                Response response = usersResource.create(userRep);

                log.info("Response from creating test user: {} {}", response.getStatus(), response.getStatusInfo());
                String userId = CreatedResponseUtil.getCreatedId(response);
                testUser.getAttributes().put(TEST_EXTERN_ID_ATT_KEY, userId);
            }

        }
    }


    public User getTestUser(boolean admin, boolean persisted) {
        User.Builder builder = User.builder();

        builder.username(this.faker.name().username());
        builder.firstName(this.faker.name().firstName());
        builder.lastName(this.faker.name().lastName());
        builder.email(this.faker.internet().emailAddress());
        builder.title(this.faker.company().profession());
        builder.roles(new ArrayList<>() {{
            add("user");
            if (admin) {
                add("userAdmin");
            }
        }});

        String password = RandomStringUtils.random(16);//TODO:: better for pw validation
        builder.pwHash(this.passwordService.createPasswordHash(password));

        User user = builder.build();

        user.getAttributes().put(TEST_PASSWORD_ATT_KEY, password);

        if (persisted) {
            this.persistTestUser(user);
        }

        return user;
    }

    public User getTestUser(boolean admin) {
        return this.getTestUser(admin, true);
    }

    public User getTestUser() {
        return this.getTestUser(false, false);
    }

    public User getExternalTestUser() {
        User testUser = this.getTestUser(false, false);

        testUser.setId(ObjectId.get());

        return testUser;
    }
}
