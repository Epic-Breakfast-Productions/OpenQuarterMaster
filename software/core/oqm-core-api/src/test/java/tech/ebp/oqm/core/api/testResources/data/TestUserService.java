package tech.ebp.oqm.core.api.testResources.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.specification.RequestSpecification;
import io.smallrye.jwt.build.Jwt;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.jwt.Claims;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;

import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.core.api.testResources.TestRestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static tech.ebp.oqm.core.api.model.object.ObjectUtils.OBJECT_MAPPER;

/**
 *
 * {
 *   "exp": 1706447044,
 *   "iat": 1706445545,
 *   "auth_time": 1706445544,
 *   "jti": "0c2d411d-1012-499e-a548-6919f384084a",
 *   "iss": "http://oqm-dev.local:8115/realms/oqm",
 *   "aud": "oqm-base-station",
 *   "sub": "575eb08f-7a8a-41cc-ac87-c41f84e03c84",
 *   "typ": "ID",
 *   "azp": "oqm-base-station",
 *   "session_state": "cfa63d15-f520-4e90-a204-9cafb5cc5621",
 *   "at_hash": "ry6laHfVyN7hlYTBzpmTAA",
 *   "acr": "1",
 *   "sid": "cfa63d15-f520-4e90-a204-9cafb5cc5621",
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
	
	public String getUserToken(User testUser) {
		String token =
			Jwt.issuer(testUser.getAuthProvider())
				.upn(testUser.getUsername())
				.groups(testUser.getRoles())
				.claim(Claims.email, testUser.getEmail())
				.claim(Claims.preferred_username, testUser.getName())
				.claim("name", testUser.getName())
				.subject(testUser.getIdFromAuthProvider())
				.sign(
					ConfigProvider.getConfig().getValue("smallrye.jwt.sign.key.location", String.class)
				)

			;
		return token;
	}

	public RequestSpecification newJwtCall(User testUser) {
		return TestRestUtils.newJwtCall(this.getUserToken(testUser));
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
}
