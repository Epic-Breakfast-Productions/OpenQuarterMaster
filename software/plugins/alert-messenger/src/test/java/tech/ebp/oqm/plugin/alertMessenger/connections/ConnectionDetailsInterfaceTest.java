package tech.ebp.oqm.plugin.alertMessenger.connections;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.models.InteractingEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ConnectionDetailsInterfaceTest {

	private static final String TOKEN = "Bearer test-token";
	private static final String USER_ID = "test-user";

	@InjectMock
	ConnectionDetailsRepository repository;

	@InjectMock
	@RestClient
	OqmCoreApiClientService coreApiClient;

	@Test
	@TestSecurity(user = "test-user")
	void getAll() {
		this.mockCurrentUser();
		PanacheQuery query = mock(PanacheQuery.class);
		when(repository.findAllPagedByUserId(USER_ID, 0, 20))
			.thenReturn(query);
		when(query.list()).thenReturn(List.of());

		given()
			.header("Authorization", TOKEN)
			.accept(ContentType.JSON)
			.when()
			.get("/api/connection-details")
			.then()
			.statusCode(200)
			.contentType(ContentType.JSON)
			.body("size()", Matchers.is(0));

		verify(coreApiClient).interactingEntityGetSelf(TOKEN);
		verify(repository).findAllPagedByUserId(USER_ID, 0, 20);
	}

	@Test
	void getAllWithoutAuth() {
		given()
			.accept(ContentType.JSON)
			.when()
			.get("/api/connection-details")
			.then()
			.statusCode(401);
	}

	@Test
	@TestSecurity(user = "test-user")
	void create() {
		this.mockCurrentUser();
		doAnswer(invocation -> {
			ConnectionDetails saved = invocation.getArgument(0);
			saved.id = new ObjectId();
			return null;
		}).when(repository).persist(any(ConnectionDetails.class));

		given()
			.header("Authorization", TOKEN)
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
	@TestSecurity(user = "test-user")
	void getById() {
		this.mockCurrentUser();
		String id = new ObjectId().toHexString();
		EmailConnection connection = emailConnection("some email");
		when(repository.findByObjectId(id)).thenReturn(Optional.of(connection));

		given()
			.header("Authorization", TOKEN)
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
	@TestSecurity(user = "test-user")
	void update() {
		this.mockCurrentUser();
		String id = new ObjectId().toHexString();
		EmailConnection existing = emailConnection("Old name");
		existing.id = new ObjectId(id);
		when(repository.findByObjectId(id)).thenReturn(Optional.of(existing));

		given()
			.header("Authorization", TOKEN)
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

		verify(repository).update(existing);
	}

	@Test
	@TestSecurity(user = "test-user")
	void delete() {
		this.mockCurrentUser();
		String id = new ObjectId().toHexString();
		EmailConnection existing = emailConnection("To delete");
		existing.id = new ObjectId(id);
		when(repository.findByObjectId(id)).thenReturn(Optional.of(existing));

		given()
			.header("Authorization", TOKEN)
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
		when(coreApiClient.interactingEntityGetSelf(TOKEN))
			.thenReturn(Uni.createFrom().item(entity));
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
