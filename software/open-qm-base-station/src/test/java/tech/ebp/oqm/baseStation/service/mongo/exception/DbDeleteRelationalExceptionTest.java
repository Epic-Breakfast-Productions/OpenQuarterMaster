package tech.ebp.oqm.baseStation.service.mongo.exception;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.testClasses.WebServerTest;
import tech.ebp.oqm.lib.core.object.MainObject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertTrue;

@Slf4j
@QuarkusTest
class DbDeleteRelationalExceptionTest extends WebServerTest {
	
	@Test
	public void testMessage(){
		ObjectId objectId = ObjectId.get();
		Map<String, Set<ObjectId>> references = Map.of(
			FAKER.name().name(), new TreeSet<>(List.of(ObjectId.get()))
		);
		
		DbDeleteRelationalException e = new DbDeleteRelationalException(new MainObject(objectId){}, references);
		
		log.info("Error message: {}", e.getMessage());
		
		assertTrue(e.getMessage().contains(objectId.toString()));
		
		for(Map.Entry<String, Set<ObjectId>> curEntry : references.entrySet()){
			assertTrue(e.getMessage().contains(curEntry.getKey()));
			
			for(ObjectId curId : curEntry.getValue()){
				assertTrue(e.getMessage().contains(curId.toString()));
			}
		}
	}
}