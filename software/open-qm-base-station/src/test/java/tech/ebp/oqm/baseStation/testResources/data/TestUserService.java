package tech.ebp.oqm.baseStation.testResources.data;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import tech.ebp.oqm.baseStation.service.JwtService;
import tech.ebp.oqm.baseStation.service.PasswordService;
import tech.ebp.oqm.baseStation.service.mongo.UserService;
import tech.ebp.oqm.baseStation.utils.AuthMode;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static tech.ebp.oqm.baseStation.testResources.lifecycleManagers.Utils.HOST_TESTCONTAINERS_INTERNAL;
import static tech.ebp.oqm.baseStation.utils.AuthMode.EXTERNAL;
import static tech.ebp.oqm.baseStation.utils.AuthMode.SELF;

@ApplicationScoped
@Slf4j
public class TestUserService {
	private final static Faker FAKER = new Faker();
	public static final String TEST_PASSWORD_ATT_KEY = "TEST_PASSWORD";
	public static final String TEST_EXTERN_ID_ATT_KEY = "TEST_EXTERNAL_KEY";
	
	private final UserService userService;
	private final MongoTestConnector mongoTestConnector = new MongoTestConnector();
	private final PasswordService passwordService = new PasswordService();
	
	private final JwtService jwtService;
	
	private final AuthMode authMode = ConfigProvider.getConfig().getValue("service.authMode", AuthMode.class);
	
	private final String keycloakAdminName = ConfigProvider.getConfig().getValue("test.keycloak.adminName", String.class);
	private final String keycloakAdminPass = ConfigProvider.getConfig().getValue("test.keycloak.adminPass", String.class);
	private final String keycloakRealm = ConfigProvider.getConfig().getValue("service.externalAuth.realm", String.class);
	private final String keycloakClientId = ConfigProvider.getConfig().getValue("service.externalAuth.clientId", String.class);
	private final String keycloakClientSecret = ConfigProvider.getConfig().getValue("service.externalAuth.clientSecret", String.class);
	
	public TestUserService(){
		try {
			this.jwtService = new JwtService(
				ConfigProvider.getConfig().getValue("mp.jwt.verify.privatekey.location", String.class),
				ConfigProvider.getConfig().getValue("mp.jwt.expiration.default", Long.class),
				ConfigProvider.getConfig().getValue("mp.jwt.expiration.extended", Long.class),
				ConfigProvider.getConfig().getValue("externalService.serviceTokenExpires", Long.class),
				ConfigProvider.getConfig().getValue("mp.jwt.verify.issuer", String.class)
			);
		} catch(Exception e) {
			throw new IllegalStateException("Failed to setup jwt service.", e);
		}
		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
			this.userService = new UserService(
				validatorFactory.getValidator(),
				ObjectUtils.OBJECT_MAPPER,
				this.mongoTestConnector.getClient(),
				this.mongoTestConnector.mongoDatabaseName,
				this.authMode
			);
		}
	}
	
	private static String getRandomPassword() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 16; i += 4) {
			sb.append(RandomStringUtils.random(1, "abcdefg"));
			sb.append(RandomStringUtils.random(1, "ABCDEFG"));
			sb.append(RandomStringUtils.random(1, "1234567"));
			sb.append(RandomStringUtils.random(1, "!@#$%^&"));
		}
		return sb.toString();
	}
	
	private static UserRepresentation userToRepresentation(User testUser) {
		UserRepresentation rep = new UserRepresentation();
		
		rep.setEnabled(true);
		rep.setUsername(testUser.getUsername());
		rep.setFirstName(testUser.getFirstName());
		rep.setLastName(testUser.getLastName());
		rep.setEmail(testUser.getEmail());
		rep.setEmailVerified(true);
		rep.setOrigin("tests");
		
		
		{
			//            rep.setGroups(testUser.getRoles());
			rep.setClientRoles(Map.of(
				"quartermaster",
				new ArrayList<>(testUser.getRoles())
			));
		}
		
		return rep;
	}
	
	public void persistTestUser(User testUser) {
		if (SELF.equals(this.authMode)) {
			this.persistTestUserInternal(testUser);
		} else if (EXTERNAL.equals(this.authMode)) {
			this.persistTestUserKeycloak(testUser);
		}
	}
	
	private void persistTestUserInternal(User testUser) {
//		this.userService.add(testUser, null);
		
		this.userService.add(testUser);
		
//		try(MongoClient client = this.mongoTestConnector.getClient()){
//
//			MongoDatabase db = client.getDatabase(this.mongoTestConnector.mongoDatabaseName);
//			MongoCollection<User> userCollection = db.getCollection(MongoService.getCollectionName(User.class), User.class);
//
//			userCollection.insertOne(testUser);
//		}
	}
	
	private void persistTestUserKeycloak(User testUser) {
		try (
			Keycloak keycloak = KeycloakBuilder.builder()
											   .serverUrl(
												   ConfigProvider.getConfig()
																 .getValue("test.keycloak.authUrl", String.class)
																 .replace(HOST_TESTCONTAINERS_INTERNAL, "localhost")
											   )
											   .realm("master")
											   .grantType(OAuth2Constants.PASSWORD)
											   .clientId("admin-cli")
											   .username(this.keycloakAdminName)
											   .password(this.keycloakAdminPass)
											   .build();
		) {
			
			UserRepresentation userRep = userToRepresentation(testUser);
			
			RealmResource realmResource = keycloak.realm(this.keycloakRealm);
			ClientRepresentation
				clientRepresentation =
				realmResource.clients()
							 .findAll()
							 .stream()
							 .filter(client->client.getClientId().equals(this.keycloakClientId))
							 .collect(Collectors.toList())
							 .get(0);
			ClientResource clientResource = realmResource.clients().get(clientRepresentation.getId());
			UsersResource usersResource = realmResource.users();
			
			String userId;
			try (Response response = usersResource.create(userRep);) {
				log.info("Response from creating test user: {} {}", response.getStatus(), response.getStatusInfo());
				userId = CreatedResponseUtil.getCreatedId(response);
				testUser.getAttributes().put(TEST_EXTERN_ID_ATT_KEY, userId);
				log.info("ID of user in keycloak: {}", testUser.getAttributes().get(TEST_EXTERN_ID_ATT_KEY));
			}
			
			if (usersResource.search(testUser.getUsername()).size() != 1) {
				throw new IllegalStateException("Test user cannot be found after creation!");
			}
			UserResource testUserResource = usersResource.get(userId);
			
			{
				CredentialRepresentation passwordCred = new CredentialRepresentation();
				passwordCred.setTemporary(false);
				passwordCred.setType(CredentialRepresentation.PASSWORD);
				passwordCred.setValue(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY));
				
				testUserResource.resetPassword(passwordCred);
			}
			{
				//                    UserRepresentation testUserRepresentation = testUserResource.toRepresentation();
				//                    RoleRepresentation roleRepresentation =;
				
				testUserResource.roles().clientLevel(clientRepresentation.getId()).add(
					testUser.getRoles().stream().map((String role)->{
						return clientResource.roles().list().stream()
											 .filter(element->element.getName().equals(role))
											 .collect(Collectors.toList())
											 .get(0);
					}).collect(Collectors.toList())
				);
			}
			
		}
		
	}
	
	public User getTestUser(boolean admin, boolean persisted) {
		User.Builder builder = User.builder();
		
		builder.username(FAKER.name().username());
		builder.firstName(FAKER.name().firstName());
		builder.lastName(FAKER.name().lastName());
		builder.email(FAKER.internet().emailAddress());
		builder.title(FAKER.company().profession());
		builder.disabled(false);
		builder.roles(new HashSet<>() {{
			add(Roles.USER);
			add(Roles.INVENTORY_EDIT);
			add(Roles.INVENTORY_VIEW);
			if (admin) {
				add(Roles.USER_ADMIN);
				add(Roles.INVENTORY_ADMIN);
				add(Roles.EXT_SERVICE_ADMIN);
			}
		}});
		
		String password = getRandomPassword();
		builder.pwHash(this.passwordService.createPasswordHash(password));
		
		User testUser = builder.build();
		
		testUser.getAttributes().put(TEST_PASSWORD_ATT_KEY, password);
		
		if (persisted) {
			this.persistTestUser(testUser);
		}
		log.debug("Done creating new user: {} - {} {}", testUser.getUsername(), testUser.getFirstName(), testUser.getLastName());
		
		return testUser;
	}
	
	public User getTestUser(boolean admin) {
		return this.getTestUser(admin, true);
	}
	
	public User getTestUser() {
		return this.getTestUser(false, false);
	}
	
	public String getTestUserToken(User testUser) {
		if (SELF.equals(this.authMode)) {
			return this.getTestUserTokenInternal(testUser);
		} else if (EXTERNAL.equals(this.authMode)) {
			return this.getTestUserTokenKeycloak(testUser);
		}
		throw new IllegalStateException("Should not get here");
	}
	
	private String getTestUserTokenInternal(User testUser) {
		return this.jwtService.getUserJwt(testUser, true).getToken();
	}
	
	private String getTestUserTokenKeycloak(User testUser) {
		try (
			Keycloak keycloak = KeycloakBuilder.builder()
											   .serverUrl(
												   ConfigProvider.getConfig().getValue("test.keycloak.authUrl", String.class)
												   //														   .replace(HOST_TESTCONTAINERS_INTERNAL, "localhost")
											   )
											   .realm(this.keycloakRealm)
											   .clientId(this.keycloakClientId)
											   .clientSecret(this.keycloakClientSecret)
											   .grantType(OAuth2Constants.PASSWORD)
											   .username(testUser.getUsername())
											   .password(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY))
											   .build()
		) {
			//				keycloak.realms();
			
			//Issuer (iss) claim value (http://localhost:49649/auth/realms/apps) doesn't match expected value of http://host.testcontainers.internal:49649/auth/realms/apps]
			
			AccessTokenResponse response = keycloak
											   .tokenManager()
											   .getAccessToken();
			
			log.info("Get user token response: {}", response.getSessionState());
			
			String token = response.getToken();
			log.info("Test user's token: {}", token);
			return token;
		} catch(Exception e) {
			log.error("FAILED to get token for user: ", e);
			throw e;
		}
	}
	
}
