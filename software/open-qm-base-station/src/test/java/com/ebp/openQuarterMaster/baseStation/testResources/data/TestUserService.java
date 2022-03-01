package com.ebp.openQuarterMaster.baseStation.testResources.data;

import com.ebp.openQuarterMaster.baseStation.service.JwtService;
import com.ebp.openQuarterMaster.baseStation.service.PasswordService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.utils.AuthMode;
import com.ebp.openQuarterMaster.lib.core.user.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static com.ebp.openQuarterMaster.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager.HOST_TESTCONTAINERS_INTERNAL;
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
	
	@Inject
	JwtService jwtService;
	
	@ConfigProperty(name = "service.authMode")
	AuthMode authMode;
	
	//    @ConfigProperty(name = "test.keycloak.authUrl", defaultValue = "")
	//    String keycloakAuthUrl;
	
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
	
	private UserRepresentation userToRepresentation(User testUser) {
		UserRepresentation rep = new UserRepresentation();
		
		rep.setEnabled(true);
		rep.setUsername(testUser.getUsername());
		rep.setFirstName(testUser.getFirstName());
		rep.setLastName(testUser.getLastName());
		rep.setEmail(testUser.getEmail());
		//        rep.setAttributes(Map.of("origin", List.of("tests")));
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
			this.userService.add(testUser, null);
		} else if (EXTERNAL.equals(this.authMode)) {
			try (
				Keycloak keycloak = KeycloakBuilder.builder()
												   .serverUrl(ConfigProvider.getConfig()
																			.getValue("test.keycloak.authUrl", String.class)
																			.replace(HOST_TESTCONTAINERS_INTERNAL, "localhost"))
												   .realm("master")
												   .grantType(OAuth2Constants.PASSWORD)
												   .clientId("admin-cli")
												   .username(this.keycloakAdminName)
												   .password(this.keycloakAdminPass)
												   .build();
			) {
				
				UserRepresentation userRep = this.userToRepresentation(testUser);
				
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
	}
	
	
	public User getTestUser(boolean admin, boolean persisted) {
		User.Builder builder = User.builder();
		
		builder.username(this.faker.name().username());
		builder.firstName(this.faker.name().firstName());
		builder.lastName(this.faker.name().lastName());
		builder.email(this.faker.internet().emailAddress());
		builder.title(this.faker.company().profession());
		builder.roles(new HashSet<>() {{
			add("user");
			if (admin) {
				add("userAdmin");
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
			return this.jwtService.getUserJwt(testUser, true).getToken();
		} else if (EXTERNAL.equals(this.authMode)) {
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
		throw new IllegalStateException("Should not get here");
	}
}
