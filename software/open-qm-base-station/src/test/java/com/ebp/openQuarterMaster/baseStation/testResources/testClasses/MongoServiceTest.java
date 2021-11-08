package com.ebp.openQuarterMaster.baseStation.testResources.testClasses;

import com.ebp.openQuarterMaster.baseStation.service.mongo.MongoService;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public abstract class MongoServiceTest<T extends MainObject, S extends MongoService<T>> extends RunningServerTest {

    protected abstract T getTestObject();

    protected T getTestObject(boolean withObjectId){
        T output = getTestObject();

        if(withObjectId){
            output.setId(new ObjectId());
        }

        return output;
    }

    protected void defaultListTest(S service){
        int numIn = 5;
        for(int i = 0; i < numIn; i++) {

            service.add(this.getTestObject(), TestUserService.getTestUser());
        }

        assertEquals(numIn, service.list().size());
    }

    protected void defaultCountTest(S service){
        int numIn = 5;
        for(int i = 0; i < numIn; i++){
            service.add(this.getTestObject(), TestUserService.getTestUser());
        }

        assertEquals(numIn, service.count());
    }

    protected void defaultGetObjectIdTest(S service){
        T item = this.getTestObject();

        ObjectId id = service.add(item, TestUserService.getTestUser());

        T itemGot = service.get(id);

        assertNotNull(itemGot);
        assertEquals(item, itemGot);
    }

    protected void defaultGetStringTest(S service){
        T item = this.getTestObject();

        String id = service.add(item, TestUserService.getTestUser()).toHexString();

        T itemGot = service.get(id);

        assertNotNull(itemGot);
        assertEquals(item, itemGot);
    }

    protected void defaultAddTest(S service){
        T item = getTestObject(false);

        ObjectId id = service.add(item, TestUserService.getTestUser());

        assertNotNull(item.getId());
        assertEquals(id, item.getId());

        log.info("num in collection: {}", service.list().size());
        assertEquals(1, service.list().size());
    }

    protected void defaultRemoveAllTest(S service){
        int numIn = 5;
        for(int i = 0; i < numIn; i++){
            service.add(this.getTestObject(), TestUserService.getTestUser());
        }

        assertEquals(numIn, service.removeAll());
        assertTrue(service.list().isEmpty());
    }
}
