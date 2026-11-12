package tech.ebp.oqm.plugin.alertMessenger.preferences;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.models.InteractingEntity;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserPreferencesInterfaceTest {

	private static final String TOKEN = "Bearer test-token";
	private static final String USER_ID = "test-user";

	@InjectMock
	UserPreferencesRepository repository;

	@InjectMock
	@RestClient
	OqmCoreApiClientService coreApiClient;

	@Test
	@TestSecurity(user = "test-user")
	void getReturnsPreferencesForAuthenticatedUser() {
		mockCurrentUser();
		when(repository.findAllPagedByUserId(USER_ID, 0, 20))
			.thenReturn(queryReturning(List.of(preference("Weekly alerts"))));

		given()
			.header("Authorization", TOKEN)
			.accept(ContentType.JSON)
			.when()
			.get("/api/user-preferences")
			.then()
			.statusCode(200)
			.body("size()", is(1))
			.body("[0].name", equalTo("Weekly alerts"))
			.body("[0].userId", equalTo(USER_ID));

		verify(repository).findAllPagedByUserId(USER_ID, 0, 20);
	}

	@Test
	@TestSecurity(user = "test-user")
	void createAssignsAuthenticatedUserToPreference() {
		mockCurrentUser();
		doAnswer(invocation -> {
			UserPreferences saved = invocation.getArgument(0);
			saved.id = new ObjectId();
			return null;
		}).when(repository).persist(any(UserPreferences.class));

		given()
			.header("Authorization", TOKEN)
			.contentType(ContentType.JSON)
			.body("""
				{
				  "name": "Weekly alerts",
				  "description": "Inventory updates",
				  "enabled": true,
				  "databaseId": "main",
				  "throttleTime": "PT0S",
				  "objectTypes": ["InventoryItem"],
				  "eventTypes": ["CREATE", "UPDATE"],
				  "connectionDetailIds": []
				}
				""")
			.when()
			.post("/api/user-preferences")
			.then()
			.statusCode(200)
			.body("name", equalTo("Weekly alerts"))
			.body("userId", equalTo(USER_ID))
			.body("enabled", equalTo(true));

		verify(repository).persist(any(UserPreferences.class));
	}

	@Test
	@TestSecurity(user = "test-user")
	void getByIdReturnsPreferenceOwnedByAuthenticatedUser() {
		mockCurrentUser();
		String id = new ObjectId().toHexString();
		UserPreferences preference = preference("Weekly alerts");
		when(repository.findByObjectIdAndUserId(id, USER_ID))
			.thenReturn(Optional.of(preference));

		given()
			.header("Authorization", TOKEN)
			.accept(ContentType.JSON)
			.pathParam("id", id)
			.when()
			.get("/api/user-preferences/{id}")
			.then()
			.statusCode(200)
			.body("name", equalTo("Weekly alerts"))
			.body("userId", equalTo(USER_ID));

		verify(repository).findByObjectIdAndUserId(id, USER_ID);
	}

	@Test
	@TestSecurity(user = "test-user")
	void updateChangesPreferenceOwnedByAuthenticatedUser() {
		mockCurrentUser();
		String id = new ObjectId().toHexString();
		UserPreferences existing = preference("Old name");
		existing.id = new ObjectId(id);
		when(repository.findByObjectIdAndUserId(id, USER_ID))
			.thenReturn(Optional.of(existing));

		given()
			.header("Authorization", TOKEN)
			.contentType(ContentType.JSON)
			.pathParam("id", id)
			.body("""
				{
				  "name": "New name",
				  "description": "Updated description",
				  "enabled": false,
				  "databaseId": "main",
				  "throttleTime": "PT0S",
				  "objectTypes": ["InventoryItem"],
				  "eventTypes": ["DELETE"],
				  "connectionDetailIds": []
				}
				""")
			.when()
			.put("/api/user-preferences/{id}")
			.then()
			.statusCode(200)
			.body("name", equalTo("New name"))
			.body("enabled", is(false))
			.body("userId", equalTo(USER_ID));

		verify(repository).update(existing);
	}

	@Test
	@TestSecurity(user = "test-user")
	void deleteRemovesPreferenceOwnedByAuthenticatedUser() {
		mockCurrentUser();
		String id = new ObjectId().toHexString();
		UserPreferences existing = preference("To delete");
		existing.id = new ObjectId(id);
		when(repository.findByObjectIdAndUserId(id, USER_ID))
			.thenReturn(Optional.of(existing));

		given()
			.header("Authorization", TOKEN)
			.pathParam("id", id)
			.when()
			.delete("/api/user-preferences/{id}")
			.then()
			.statusCode(204);

		verify(repository).delete(existing);
	}

	private void mockCurrentUser() {
		InteractingEntity entity = new InteractingEntity();
		entity.setId(USER_ID);
		when(coreApiClient.interactingEntityGetSelf(TOKEN))
			.thenReturn(Uni.createFrom().item(entity));
	}

	private UserPreferences preference(String name) {
		UserPreferences preference = new UserPreferences();
		preference.userId = USER_ID;
		preference.name = name;
		preference.enabled = true;
		preference.throttleTime = Duration.ZERO;
		preference.connectionDetailIds = List.of();
		return preference;
	}

	@SuppressWarnings("unchecked")
	private io.quarkus.mongodb.panache.PanacheQuery<UserPreferences> queryReturning(
		List<UserPreferences> results
	) {
		io.quarkus.mongodb.panache.PanacheQuery<UserPreferences> query = mock(
			io.quarkus.mongodb.panache.PanacheQuery.class
		);
		when(query.list()).thenReturn(results);
		return query;
	}
}
