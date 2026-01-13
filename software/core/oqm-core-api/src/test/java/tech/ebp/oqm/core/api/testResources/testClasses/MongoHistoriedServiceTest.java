package tech.ebp.oqm.core.api.testResources.testClasses;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.core.api.testResources.data.TestUserService;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.interactingEntity.user.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.ebp.oqm.core.api.testResources.TestConstants.DEFAULT_TEST_DB_NAME;

@Slf4j
public abstract class MongoHistoriedServiceTest<T extends MainObject, S extends MongoHistoriedObjectService<T, ?, ?>> extends RunningServerTest {

	protected abstract T getTestObject();
	
	protected T getTestObject(boolean withObjectId) {
		T output = getTestObject();
		
		if (withObjectId) {
			output.setId(new ObjectId());
		}
		
		return output;
	}
	
	protected void defaultListTest(S service) {
		int numIn = 5;
		for (int i = 0; i < numIn; i++) {
			
			service.add(DEFAULT_TEST_DB_NAME, this.getTestObject(), this.testUserService.getTestUser());
		}
		
		assertEquals(numIn, service.list(DEFAULT_TEST_DB_NAME).size());
	}
	
	protected void defaultCountTest(S service) {
		int numIn = 5;
		User testUser = this.testUserService.getTestUser();
		for (int i = 0; i < numIn; i++) {
			service.add(DEFAULT_TEST_DB_NAME, this.getTestObject(), testUser);
		}
		
		assertEquals(numIn, service.count(DEFAULT_TEST_DB_NAME));
	}
	
	protected void defaultGetObjectIdTest(S service) {
		T item = this.getTestObject();
		
		T newItem = service.add(DEFAULT_TEST_DB_NAME, item, this.testUserService.getTestUser());
		
		T itemGot = service.get(DEFAULT_TEST_DB_NAME, newItem.getId());
		
		assertNotNull(itemGot);
		assertEquals(item, itemGot);
		assertEquals(newItem, itemGot);
	}
	
	protected void defaultGetStringTest(S service) {
		T item = this.getTestObject();
		
		T itemAdded = service.add(DEFAULT_TEST_DB_NAME, item, this.testUserService.getTestUser());
		
		T itemGot = service.get(DEFAULT_TEST_DB_NAME, itemAdded.getId().toHexString());
		
		assertNotNull(itemGot);
		assertEquals(item, itemGot);
	}
	
	protected void defaultAddTest(S service) {
		T item = getTestObject(false);
		
		T itemAdded = service.add(DEFAULT_TEST_DB_NAME, item, this.testUserService.getTestUser());
		
		assertNotNull(item.getId());
		assertEquals(item, itemAdded);
		
		log.info("num in collection: {}", service.count(DEFAULT_TEST_DB_NAME));
		assertEquals(1, service.count(DEFAULT_TEST_DB_NAME));
	}
	
	protected void defaultRemoveAllTest(S service) {
		int numIn = 5;
		for (int i = 0; i < numIn; i++) {
			service.add(DEFAULT_TEST_DB_NAME, this.getTestObject(), this.testUserService.getTestUser());
		}
		
		assertEquals(numIn, service.removeAll(DEFAULT_TEST_DB_NAME, this.testUserService.getTestUser()));
		assertTrue(service.list(DEFAULT_TEST_DB_NAME).isEmpty());
	}
}
