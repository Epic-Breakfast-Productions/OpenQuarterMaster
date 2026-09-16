package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.interfaces.endpoints.interactingEntity.InteractingEntityEndpoints;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;
import tech.ebp.oqm.core.api.model.rest.search.InteractingEntitySearch;
import tech.ebp.oqm.core.api.service.mongo.search.SearchResult;
import tech.ebp.oqm.core.api.testResources.TestRestUtils;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.testResources.testClasses.RunningServerTest;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
@TestHTTPEndpoint(InteractingEntityEndpoints.class)
public class InteractingEntityServiceTest extends RunningServerTest {

	@Inject
	InteractingEntityService interactingEntityService;

	@Inject
	ExecutorService executorService;


	@Test
	public void ensureTest(){
		User testUser = TestUserService.getInstance().getTestUser(true, false);

		SearchResult<InteractingEntity> entityResult = this.interactingEntityService.search(new InteractingEntitySearch().setName(testUser.getName()));
		log.debug("Initial entity state: {}", entityResult);
		assertTrue(entityResult.isEmpty(), "Entity database was not clean.");

		ValidatableResponse response = TestRestUtils.setupJwtCall(given(), TestUserService.getInstance().getUserToken(testUser))
			.get()
			.then()
			.statusCode(200);
		// TODO:: test user is in response

		entityResult = this.interactingEntityService.search(new InteractingEntitySearch().setName(testUser.getName()));
		log.debug("Entity state after first call: {}", entityResult);
		assertEquals(1, entityResult.getNumResults(), "Entity database Did not contain user.");

		response = TestRestUtils.setupJwtCall(given(), TestUserService.getInstance().getUserToken(testUser))
			.get()
			.then()
			.statusCode(200);
		// TODO:: test user is in response

		entityResult = this.interactingEntityService.search(new InteractingEntitySearch().setName(testUser.getName()));
		log.debug("Entity state after first call: {}", entityResult);
		assertEquals(1, entityResult.getNumResults(), "Entity database Did not contain user only once.");
	}

	@Test
	public void ensureMultiThreadedTest(){
		User testUser = TestUserService.getInstance().getTestUser(true, false);

		SearchResult<InteractingEntity> entityResult = this.interactingEntityService.search(new InteractingEntitySearch().setName(testUser.getName()));
		log.debug("Initial entity state: {}", entityResult);
		assertTrue(entityResult.isEmpty(), "Entity database was not clean.");

		//TODO:: unsure if actually properly multithreading
		UniJoin.Builder<ValidatableResponse> multiThreadBuilder = Uni.join().builder();

		for(int i = 0; i < 50; i++){
			int finalI = i;
			multiThreadBuilder.add(
				Uni.createFrom().item(()-> {
					log.debug("Sending request {}", finalI);
					return TestRestUtils.setupJwtCall(given(), TestUserService.getInstance().getUserToken(testUser))
					.get()
					.then()
					.statusCode(200);
				})
			);
		}

		List<ValidatableResponse> responses = multiThreadBuilder.joinAll()
			.andCollectFailures()
			.runSubscriptionOn(this.executorService)
			.await().indefinitely();

		entityResult = this.interactingEntityService.search(new InteractingEntitySearch().setName(testUser.getName()));
		log.debug("Entity state after calls: {}", entityResult);
		assertEquals(1, entityResult.getNumResults(), "Entity database Did not contain user only once.");
	}


}
