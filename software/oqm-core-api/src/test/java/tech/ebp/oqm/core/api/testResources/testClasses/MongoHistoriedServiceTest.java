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

@Slf4j
public abstract class MongoHistoriedServiceTest<T extends MainObject, S extends MongoHistoriedObjectService<T, ?, ?>> extends RunningServerTest {
	
	protected TestUserService testUserService;
	
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
			
			service.add(this.getTestObject(), this.testUserService.getTestUser());
		}
		
		assertEquals(numIn, service.list().size());
	}
	
	protected void defaultCountTest(S service) {
		int numIn = 5;
		User testUser = this.testUserService.getTestUser();
		for (int i = 0; i < numIn; i++) {
			service.add(this.getTestObject(), testUser);
		}
		
		assertEquals(numIn, service.count());
	}
	
	protected void defaultGetObjectIdTest(S service) {
		T item = this.getTestObject();
		
		ObjectId id = service.add(item, this.testUserService.getTestUser());
		
		T itemGot = service.get(id);
		
		assertNotNull(itemGot);
		assertEquals(item, itemGot);
	}
	
	protected void defaultGetStringTest(S service) {
		T item = this.getTestObject();
		
		String id = service.add(item, this.testUserService.getTestUser()).toHexString();
		
		T itemGot = service.get(id);
		
		assertNotNull(itemGot);
		assertEquals(item, itemGot);
	}
	
	protected void defaultAddTest(S service) {
		T item = getTestObject(false);
		
		ObjectId id = service.add(item, this.testUserService.getTestUser());
		
		assertNotNull(item.getId());
		assertEquals(id, item.getId());
		
		log.info("num in collection: {}", service.list().size());
		assertEquals(1, service.list().size());
	}
	
	protected void defaultRemoveAllTest(S service) {
		int numIn = 5;
		for (int i = 0; i < numIn; i++) {
			service.add(this.getTestObject(), this.testUserService.getTestUser());
		}
		
		assertEquals(numIn, service.removeAll(this.testUserService.getTestUser()));
		assertTrue(service.list().isEmpty());
	}
}
