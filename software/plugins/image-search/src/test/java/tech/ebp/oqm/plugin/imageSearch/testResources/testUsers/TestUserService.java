package tech.ebp.oqm.plugin.imageSearch.testResources.testUsers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {
 * "exp": 1706447044,
 * "iat": 1706445545,
 * "auth_time": 1706445544,
 * "jti": "0c2d411d-1012-499e-a548-6919f384084a",
 * "iss": "http://oqm-dev.local:8115/realms/oqm",
 * "aud": "oqm-base-station",
 * "sub": "575eb08f-7a8a-41cc-ac87-c41f84e03c84",
 * "typ": "ID",
 * "azp": "oqm-base-station",
 * "session_state": "cfa63d15-f520-4e90-a204-9cafb5cc5621",
 * "at_hash": "ry6laHfVyN7hlYTBzpmTAA",
 * "acr": "1",
 * "sid": "cfa63d15-f520-4e90-a204-9cafb5cc5621",
 * "upn": "snappawapa",
 * "email_verified": false,
 * "name": "Greg Stewart",
 * "groups": [
 * "default-roles-oqm",
 * "inventoryView",
 * "offline_access",
 * "itemCheckout",
 * "inventoryEdit",
 * "uma_authorization",
 * "inventoryAdmin",
 * "user"
 * ],
 * "preferred_username": "snappawapa",
 * "given_name": "Greg",
 * "family_name": "Stewart",
 * "email": "contact@gjstewart.net"
 * }
 */
@Slf4j
@NoArgsConstructor
public class TestUserService {
	private final static Faker FAKER = new Faker();

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

	private final static TestUserService INSTANCE = new TestUserService();

	public static TestUserService getInstance() {
		return INSTANCE;
	}

	private Map<TestUserType, TestUser> testUsers = new HashMap<>();

	public TestUser getTestUser(TestUserType type) {
		if (!this.testUsers.containsKey(type)) {
			testUsers.put(
				type,
				TestUser.builder()
					.email(FAKER.internet().emailAddress())
					.firstname(FAKER.name().firstName())
					.lastname(FAKER.name().lastName())
					.password(getRandomPassword())
					.build()
			);
		}

		return this.testUsers.get(type);
	}

	public TestUser getTestUser() {
		return this.getTestUser(TestUserType.REGULAR);
	}

	public List<TestUser> getAllTestUsers(){
		return Arrays.stream(TestUserType.values())
			.map(this::getTestUser)
			.toList();
	}
}
