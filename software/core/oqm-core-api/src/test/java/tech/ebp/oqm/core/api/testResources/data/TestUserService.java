package tech.ebp.oqm.core.api.testResources.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.Claims;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.GeneralService;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;

import tech.ebp.oqm.core.api.service.JwtUtils;
import tech.ebp.oqm.core.api.testResources.TestRestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

/**
 * Service for providing test users and service accounts for use in testing.
 */
@Slf4j
public class TestUserService {
	private final static Faker FAKER = new Faker();
	public static final String TEST_PASSWORD_ATT_KEY = "TEST_PASSWORD";
	public static final String TEST_JWT_ATT_KEY = "TEST_JWT";
	private static final String TEST_EXTERN_ID_ATT_KEY = "TEST_KEYCLOAK_ID";

	private final static TestUserService INSTANCE = new TestUserService();
	public static TestUserService getInstance() {
		return INSTANCE;
	}

	private final String jwtIssuer = ConfigProvider.getConfig().getValue("mp.jwt.verify.issuer", String.class);

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

	private JwtClaimsBuilder getBasicJwtBuilder(InteractingEntity entity){
		return Jwt.issuer(entity.getAuthProvider())
				   .groups(entity.getRoles())
				   .subject(entity.getIdFromAuthProvider());
	}

	private String finalizeNewToken(JwtClaimsBuilder builder){
		return builder.sign(ConfigProvider.getConfig().getValue("smallrye.jwt.sign.key.location", String.class));
	}

	/**
	 * Should look like:
	 *
	 * <code>
	 *     {
	 *   "exp": 1788903348,
	 *   "iat": 1788901848,
	 *   "auth_time": 1787934573,
	 *   "jti": "onrtac:d5f008c3-d219-e090-2129-ce541d005a0f",
	 *   "iss": "https://oqm.localdomain/infra/keycloak/realms/oqm",
	 *   "aud": "account",
	 *   "sub": "57d9a59b-4af0-4b14-8220-39b29fae1a2e",
	 *   "typ": "Bearer",
	 *   "azp": "oqm-base-station",
	 *   "sid": "tqtgtN9qZm1l1UX0Nmeo3mjd",
	 *   "acr": "0",
	 *   "realm_access": {
	 *     "roles": [
	 *       "default-roles-oqm",
	 *       "inventoryView",
	 *       "offline_access",
	 *       "itemCheckout",
	 *       "inventoryEdit",
	 *       "uma_authorization",
	 *       "inventoryAdmin",
	 *       "user"
	 *     ]
	 *   },
	 *   "resource_access": {
	 *     "account": {
	 *       "roles": [
	 *         "manage-account",
	 *         "manage-account-links",
	 *         "view-profile"
	 *       ]
	 *     }
	 *   },
	 *   "scope": "openid email microprofile-jwt profile",
	 *   "upn": "snappawapa",
	 *   "email_verified": false,
	 *   "name": "Greg Stewart",
	 *   "groups": [
	 *     "default-roles-oqm",
	 *     "inventoryView",
	 *     "offline_access",
	 *     "itemCheckout",
	 *     "inventoryEdit",
	 *     "uma_authorization",
	 *     "inventoryAdmin",
	 *     "user"
	 *   ],
	 *   "preferred_username": "snappawapa",
	 *   "given_name": "Greg",
	 *   "family_name": "Stewart",
	 *   "email": "contact@gjstewart.net"
	 * }
	 * </code>
	 * @param testUser
	 * @return
	 */
	public String getUserToken(User testUser) {
		JwtClaimsBuilder builder = this.getBasicJwtBuilder(testUser)
									   .upn(
										   testUser.getUsername() == null?
											   testUser.getName() :
											   testUser.getUsername()
									   )
									   .claim(JwtUtils.CLAIM_NAME, testUser.getName());

		if(testUser.getEmail() != null){
			builder = builder.claim(Claims.email, testUser.getEmail());
		}
		if(testUser.getUsername() != null){
			builder = builder.claim(Claims.preferred_username, testUser.getUsername());
		}

		return this.finalizeNewToken(builder);
	}

	/**
	 * Should look like:
	 *
	 * <code>
	 *     {
	 *   "exp": 1788976935,
	 *   "iat": 1788975435,
	 *   "jti": "trrtcc:4e97796a-82d6-c14c-097d-d914f057b15e",
	 *   "iss": "https://oqm-test-ud-24-04.local/infra/keycloak/realms/oqm",
	 *   "aud": "account",
	 *   "sub": "66f3849c-c867-4bc3-b317-e78f96357ea8",
	 *   "typ": "Bearer",
	 *   "azp": "oqm-base-station",
	 *   "acr": "1",
	 *   "realm_access": {
	 *     "roles": [
	 *       "default-roles-oqm",
	 *       "inventoryView",
	 *       "offline_access",
	 *       "itemCheckout",
	 *       "inventoryEdit",
	 *       "uma_authorization",
	 *       "inventoryAdmin",
	 *       "user"
	 *     ]
	 *   },
	 *   "resource_access": {
	 *     "account": {
	 *       "roles": [
	 *         "manage-account",
	 *         "manage-account-links",
	 *         "view-profile"
	 *       ]
	 *     }
	 *   },
	 *   "scope": "email microprofile-jwt profile",
	 *   "upn": "service-account-oqm-base-station",
	 *   "email_verified": false,
	 *   "clientHost": "172.18.0.1",
	 *   "groups": [
	 *     "default-roles-oqm",
	 *     "inventoryView",
	 *     "offline_access",
	 *     "itemCheckout",
	 *     "inventoryEdit",
	 *     "uma_authorization",
	 *     "inventoryAdmin",
	 *     "user"
	 *   ],
	 *   "preferred_username": "service-account-oqm-base-station",
	 *   "clientAddress": "172.18.0.1",
	 *   "client_id": "oqm-base-station"
	 * }
	 * </code>
	 *
	 * @param testUser
	 * @return
	 */
	public String getServiceToken(GeneralService testUser) {
		JwtClaimsBuilder builder = this.getBasicJwtBuilder(testUser)
									   .upn("service-account-" + testUser.getName())
									   .claim(Claims.azp, testUser.getName());

		if(testUser.getEmail() != null){
			builder = builder.claim(JwtUtils.CLAIM_DEV_EMAIL, testUser.getEmail());
		}
		if(testUser.getDeveloperName() != null){
			builder = builder.claim(JwtUtils.CLAIM_DEV_NAME, testUser.getDeveloperName());
		}
		if(testUser.getDeveloperWebsite() != null){
			builder = builder.claim(JwtUtils.CLAIM_DEV_WEBSITE, testUser.getDeveloperWebsite());
		}

		return this.finalizeNewToken(builder);
	}

	public RequestSpecification newJwtCall(InteractingEntity testUser) {
		return TestRestUtils.newJwtCall(
			switch (testUser.getType()){
				case USER -> this.getUserToken((User) testUser);
				case SERVICE_GENERAL -> this.getServiceToken((GeneralService) testUser);
				case CORE_API -> null;
			}
		);
	}

	public User getTestUser(Set<String> roles, boolean create) {
		User.UserBuilder builder = User.builder();

		builder.username(FAKER.credentials().username());
		builder.email(FAKER.internet().emailAddress());
		builder.name(FAKER.name().fullName());
		builder.roles(roles);
		User testUser = builder.build();

		testUser.setAuthProvider(this.jwtIssuer);
		testUser.setIdFromAuthProvider(UUID.randomUUID().toString());

		testUser.getAttributes().put(TEST_PASSWORD_ATT_KEY, getRandomPassword());

		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getUserToken(testUser));

		if(create) {
			//ensure user is added to db
			String userJsonString = this.newJwtCall(testUser)
				.basePath("")
				.get("/api/v1/interacting-entity/self")
				.then()
				.statusCode(200)
				.extract().body().asString();
			try {
				ObjectNode userJson = (ObjectNode) OBJECT_MAPPER.readTree(userJsonString);
				testUser.setId(new ObjectId(userJson.get("id").asText()));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

		return testUser;
	}

	public User getTestUser(String ... roles) {
		return this.getTestUser(Set.of(roles), true);
	}

	public User getTestUser(boolean admin, boolean create) {
		Set<String> roles = new HashSet<>(Roles.NON_ADMIN_ROLES);

		if(admin){
			roles.addAll(Roles.ADMIN_ROLES);
		}

		return this.getTestUser(roles, create);
	}

	public User getTestUser(boolean admin) {
		return this.getTestUser(admin, true);
	}

	public User getTestUser(){
		return this.getTestUser(true);
	}

	public GeneralService getServiceAccount(boolean create){
		GeneralService.GeneralServiceBuilder<?, ?> builder = GeneralService.builder();

		builder.name("service-account-" + FAKER.internet().domainName());
		builder.developerEmail(FAKER.internet().emailAddress());
		builder.developerName(FAKER.name().fullName());
		builder.developerWebsite(FAKER.internet().url());
		GeneralService testUser = builder.build();

		testUser.setAuthProvider(this.jwtIssuer);
		testUser.setIdFromAuthProvider(UUID.randomUUID().toString());

		testUser.getAttributes().put(TEST_PASSWORD_ATT_KEY, getRandomPassword());

		testUser.getAttributes().put(TEST_JWT_ATT_KEY, this.getServiceToken(testUser));

		if(create) {
			//ensure user is added to db
			String userJsonString = this.newJwtCall(testUser)
										.basePath("")
										.get("/api/v1/interacting-entity/self")
										.then()
										.statusCode(200)
										.extract().body().asString();
			try {
				ObjectNode userJson = (ObjectNode) OBJECT_MAPPER.readTree(userJsonString);
				testUser.setId(new ObjectId(userJson.get("id").asText()));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

		return testUser;
	}


}
