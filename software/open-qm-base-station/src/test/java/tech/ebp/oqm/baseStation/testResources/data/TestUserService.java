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
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import tech.ebp.oqm.baseStation.service.PasswordService;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.user.User;
import tech.ebp.oqm.baseStation.model.rest.auth.roles.Roles;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * TODO:: 361 replace this functionality
 */
@ApplicationScoped
@Slf4j
public class TestUserService {
	private final static Faker FAKER = new Faker();
	public static final String TEST_PASSWORD_ATT_KEY = "TEST_PASSWORD";
	public static final String TEST_JWT_ATT_KEY = "TEST_JWT";
	private static final String TEST_EXTERN_ID_ATT_KEY = "TEST_KEYCLOAK_ID";
	
	private final MongoTestConnector mongoTestConnector = new MongoTestConnector();
	private final PasswordService passwordService = new PasswordService();
	
	private final String keycloakAdminName = ConfigProvider.getConfig().getValue("quarkus.keycloak.admin-client.username", String.class);
	private final String keycloakAdminPass = ConfigProvider.getConfig().getValue("quarkus.keycloak.admin-client.password", String.class);
	private final String keycloakRealm = ConfigProvider.getConfig().getValue("quarkus.keycloak.devservices.realm-name", String.class);
	private final String keycloakUrl = ConfigProvider.getConfig().getValue("quarkus.oidc.auth-server-url", String.class).replace("realms/"+keycloakRealm, "");
	private final String keycloakClientId = ConfigProvider.getConfig().getValue("quarkus.oidc.client-id", String.class);
	private final String keycloakClientSecret = ConfigProvider.getConfig().getValue("quarkus.oidc.credentials.secret", String.class);
	
//	quarkus.oidc.application-type=hybrid
//	quarkus.oidc.auth-server-url=http://localhost:32769/realms/oqm
//	quarkus.oidc.client-id=oqm-app
//	quarkus.oidc.credentials.secret=**********
//	quarkus.oidc.logout.path=/logout
//	quarkus.oidc.logout.post-logout-path=/
//	quarkus.oidc.token-state-manager.split-tokens=true
	
	public TestUserService(){
	
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
		rep.setFirstName(testUser.getName().split(" ")[0]);
		rep.setLastName(testUser.getName().split(" ")[1]);
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
	
	private Keycloak getAdminClient(String realm){
		return KeycloakBuilder.builder()
								.serverUrl(this.keycloakUrl)
								.realm(realm)
								.grantType(OAuth2Constants.PASSWORD)
								.clientId("admin-cli")
								.username(this.keycloakAdminName)
								.password(this.keycloakAdminPass)
								.build();
	}
	
	private void persistTestUser(User testUser) {
		try (
			Keycloak keycloak = this.getAdminClient("master");
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
				
				
				testUserResource.roles().realmLevel().add(
					testUser.getRoles().stream().map((String role)->{
						List<RoleRepresentation> representationList = realmResource.roles().list().stream()
								   .filter(element->element.getName().equals(role))
								   .collect(Collectors.toList());
						
						if(representationList.isEmpty()){
							return null;
						}
						return representationList.get(0);
					})
						.filter(Objects::nonNull)
						.collect(Collectors.toList())
				);
				
//				testUserResource.roles().clientLevel(clientRepresentation.getId()).add(
//					testUser.getRoles().stream().map((String role)->{
//						return clientResource.roles().list().stream()
//											 .filter(element->element.getName().equals(role))
//											 .collect(Collectors.toList())
//											 .get(0);
//					}).collect(Collectors.toList())
//				);
			}

		}
		
	}
	
	private String getTestUserTokenKeycloak(User testUser) {
		try (
			Keycloak keycloak = KeycloakBuilder.builder()
									.serverUrl(this.keycloakUrl)
									.realm(this.keycloakRealm)
									.clientId(this.keycloakClientId)
									.clientSecret(this.keycloakClientSecret)
									.grantType(OAuth2Constants.PASSWORD)
									.username(testUser.getUsername())
									.password(testUser.getAttributes().get(TEST_PASSWORD_ATT_KEY))
									.build()
		) {
			keycloak.realms();
			
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
	
	public User getTestUser(Set<String> roles) {
		User.Builder builder = User.builder();
		
		builder.username(FAKER.name().username());
		builder.email(FAKER.internet().emailAddress());
		builder.name(FAKER.name().fullName());
		builder.roles(roles);
		User testUser = builder.build();
		
		testUser.getAttributes().put(TEST_PASSWORD_ATT_KEY, getRandomPassword());
		
		this.persistTestUser(testUser);
		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getTestUserTokenKeycloak(testUser));
		
		return testUser;
	}
	
	public User getTestUser(String ... roles) {
		return this.getTestUser(Set.of(roles));
	}
	
	public User getTestUser(boolean admin) {
		Set<String> roles = new HashSet<>(Roles.NON_ADMIN_ROLES);
		
		if(admin){
			roles.addAll(Roles.ADMIN_ROLES);
		}
		
		return this.getTestUser(roles);
	}
	
	public User getTestUser(){
		return this.getTestUser(true);
	}
}
