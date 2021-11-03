package com.ebp.openQuarterMaster.baseStation.endpoints.inventory.items;

import com.ebp.openQuarterMaster.baseStation.service.mongo.InventoryItemService;
import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.data.InventoryItemTestObjectCreator;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.storage.InventoryItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
@TestHTTPEndpoint(InventoryItemsCrud.class)
class InventoryItemsCrudTest extends RunningServerTest {
    @Inject
    InventoryItemTestObjectCreator testObjectCreator;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    InventoryItemService service;


    //    @Test
    public void testCreate() throws JsonProcessingException {
        InventoryItem item = testObjectCreator.getTestObject();
        ObjectId returned = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(item))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .extract().body().as(ObjectId.class);
        log.info("Got object id back from create request: {}", returned);

        InventoryItem stored = service.get(returned);
        assertNotNull(stored);

        assertFalse(stored.getHistory().isEmpty());
        assertEquals(1, stored.getHistory().size());

        item.setHistory(stored.getHistory());
        item.setId(returned);


        assertEquals(item, stored);
    }
}