package tech.ebp.oqm.plugin.alertMessenger.connections;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.models.InteractingEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ConnectionDetailsInterfaceTest {

	private static final String USER_ID = "test-user";

	@Inject
	KcClientAuthService keycloakAuthService;

	@InjectMock
	ConnectionDetailsRepository repository;

	@InjectMock
	@RestClient
	OqmCoreApiClientService coreApiClient;

	@Test
	void getAll() {
		this.mockCurrentUser();
		PanacheQuery query = mock(PanacheQuery.class);
		when(repository.findAllPagedByUserId(USER_ID, 0, 20))
			.thenReturn(query);
		when(query.list()).thenReturn(List.of());

		given()
			.header("Authorization", authHeader())
			.accept(ContentType.JSON)
			.when()
			.get("/api/connection-details")
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("size()", Matchers.is(0));

		verify(coreApiClient).interactingEntityGetSelf(authHeader());
		verify(repository).findAllPagedByUserId(USER_ID, 0, 20);
	}

	@Test
	void getAllWithoutAuth() {
		given()
			.redirects().follow(false)
			.accept(ContentType.JSON)
			.when()
			.get("/api/connection-details")
			.then()
			.statusCode(302);
	}

	@Test
	void create() {
		this.mockCurrentUser();
		doAnswer(invocation -> {
			ConnectionDetails saved = invocation.getArgument(0);
			saved.id = new ObjectId();
			return null;
		}).when(repository).persist(any(ConnectionDetails.class));

		given()
			.header("Authorization", authHeader())
			.contentType(ContentType.JSON)
			.body("""
				{
				  "type": "EMAIL",
				  "name": "some email",
				  "description": "Receives inventory alerts",
				  "destinations": ["test@oqm.com"],
				  "cc": [],
				  "bcc": []
				}
				""")
		.when()
			.post("/api/connection-details")
		.then()
			.statusCode(200)
			.body("type", equalTo("EMAIL"))
			.body("name", equalTo("some email"))
			.body("userId", equalTo(USER_ID));

		verify(repository).persist(any(ConnectionDetails.class));
	}

	@Test
	void createIgnoresUserIdFromRequest() {
		this.mockCurrentUser();
		doAnswer(invocation -> {
			ConnectionDetails saved = invocation.getArgument(0);
			saved.id = new ObjectId();
			return null;
		}).when(repository).persist(any(ConnectionDetails.class));

		given()
			.header("Authorization", authHeader())
			.contentType(ContentType.JSON)
			.body("""
				{
					"userId": "random-string",
					"type": "EMAIL",
					"name": "some email",
					"description": "Receives inventory alerts",
					"destinations": ["test@oqm.com"],
					"cc": [],
					"bcc": []
				}
				""")
			.when()
			.post("/api/connection-details")
			.then()
			.statusCode(200)
			.body("type", equalTo("EMAIL"))
			.body("name", equalTo("some email"))
			.body("userId", equalTo(USER_ID));

		verify(repository).persist(any(ConnectionDetails.class));
	}

	@Test
	void getById() {
		this.mockCurrentUser();
		String id = new ObjectId().toHexString();
		EmailConnection connection = emailConnection("some email");
		when(repository.findByObjectId(id)).thenReturn(Optional.of(connection));

		given()
			.header("Authorization", authHeader())
			.accept(ContentType.JSON)
			.pathParam("id", id)
		.when()
			.get("/api/connection-details/{id}")
		.then()
			.statusCode(200)
			.body("type", equalTo("EMAIL"))
			.body("name", equalTo("some email"))
			.body("userId", equalTo(USER_ID));

		verify(repository).findByObjectId(id);
	}

	@Test
	void update() {
		this.mockCurrentUser();
		String id = new ObjectId().toHexString();
		EmailConnection existing = emailConnection("Old name");
		existing.id = new ObjectId(id);
		when(repository.findByObjectId(id)).thenReturn(Optional.of(existing));

		given()
			.header("Authorization", authHeader())
			.contentType(ContentType.JSON)
			.pathParam("id", id)
			.body("""
				{
				  "type": "EMAIL",
				  "name": "New name",
				  "description": "Updated description",
				  "destinations": ["new-test@oqm.com"],
				  "cc": [],
				  "bcc": []
				}
				""")
		.when()
			.put("/api/connection-details/{id}")
		.then()
			.statusCode(200)
			.body("type", equalTo("EMAIL"))
			.body("name", equalTo("New name"))
			.body("userId", equalTo(USER_ID));

		ArgumentCaptor<ConnectionDetails> updatedCaptor = ArgumentCaptor.forClass(ConnectionDetails.class);
		verify(repository).update(updatedCaptor.capture());
		assertEquals(existing.id, updatedCaptor.getValue().id);
		assertEquals(USER_ID, updatedCaptor.getValue().getUserId());
		assertEquals("New name", updatedCaptor.getValue().getName());
	}

	@Test
	void updateConnectionOwnedByAnotherUser() {
		this.mockDifferentUser();
		String id = new ObjectId().toHexString();
		EmailConnection existing = emailConnection("Old name");
		existing.id = new ObjectId(id);
		when(repository.findByObjectId(id)).thenReturn(Optional.of(existing));

		given()
			.header("Authorization", authHeader())
			.contentType(ContentType.JSON)
			.pathParam("id", id)
			.body("""
				{
				  "type": "EMAIL",
				  "name": "New name",
				  "description": "Updated description",
				  "destinations": ["new-test@oqm.com"],
				  "cc": [],
				  "bcc": []
				}
				""")
			.when()
			.put("/api/connection-details/{id}")
			.then()
			.statusCode(404);

		verify(repository).findByObjectId(id);
		verify(repository, never()).update(any(ConnectionDetails.class));
	}

	@Test
	void delete() {
		this.mockCurrentUser();
		String id = new ObjectId().toHexString();
		EmailConnection existing = emailConnection("To delete");
		existing.id = new ObjectId(id);
		when(repository.findByObjectId(id)).thenReturn(Optional.of(existing));

		given()
			.header("Authorization", authHeader())
			.pathParam("id", id)
		.when()
			.delete("/api/connection-details/{id}")
		.then()
			.statusCode(204);

		verify(repository).delete(existing);
	}

	private void mockCurrentUser() {
		InteractingEntity entity = new InteractingEntity();
		entity.setId(USER_ID);
		when(coreApiClient.interactingEntityGetSelf(authHeader()))
			.thenReturn(Uni.createFrom().item(entity));
	}

	private void mockDifferentUser() {
		InteractingEntity entity = new InteractingEntity();
		entity.setId("different-user");
		when(coreApiClient.interactingEntityGetSelf(authHeader()))
			.thenReturn(Uni.createFrom().item(entity));
	}

	private String authHeader() {
		return keycloakAuthService.getAuthString();
	}

	// how to create any ConnectionDetails subclass instance for testing?
	// and if we create new subclass, we need to add a new method here to create it for testing?
	// need find a better way to create ConnectionDetails subclass instances for testing
	private EmailConnection emailConnection(String name) {
		EmailConnection connection = new EmailConnection();
		connection.setUserId(USER_ID);
		connection.setName(name);
		connection.setDestinations(Set.of("test@oqm.com"));
		return connection;
	}
}
