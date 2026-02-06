package tech.ebp.oqm.core.api.service.mongo.exception;

import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.testResources.testClasses.WebServerTest;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.exception.db.DbDeleteRelationalException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
class DbDeleteRelationalExceptionTest extends WebServerTest {
	
	@Test
	public void testMessage(){
		ObjectId objectId = ObjectId.get();
		Map<String, Set<ObjectId>> references = Map.of(
			FAKER.name().name(), new TreeSet<>(List.of(ObjectId.get()))
		);
		
		DbDeleteRelationalException e = new DbDeleteRelationalException(new MainObject(objectId){
			@Override
			public int getSchemaVersion() {
				return 1;
			}
		}, references);
		
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