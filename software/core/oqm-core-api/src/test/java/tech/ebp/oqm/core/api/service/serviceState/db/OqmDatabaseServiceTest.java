package tech.ebp.oqm.core.api.service.serviceState.db;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.core.api.testResources.testClasses.WebServerTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@QuarkusTest
class OqmDatabaseServiceTest extends WebServerTest {

	@Inject
	OqmDatabaseService databaseService;

	@Test
	public void cacheMatchesRealityTest(){
		List<OqmMongoDatabase> databases = databaseService.listIterator().into(new ArrayList<>());
		List<OqmMongoDatabase> cachedDbs = this.databaseService.getDatabaseCache().getDbCache().stream().map(DbCacheEntry::getOqmMongoDatabase).collect(Collectors.toList());

		log.info("Databases in Mongodb: {}", databases);
		log.info("Databases in cache: {}", cachedDbs);
		assertEquals(databases.size(), cachedDbs.size());
	}

	@Test
	public void addTest(){
		OqmMongoDatabase newDatabaseNew = OqmMongoDatabase.builder()
			.name("newDb")
			.description(FAKER.lorem().sentence())
			.build();
		ObjectId newDatabaseId = this.databaseService.addOqmDatabase(newDatabaseNew);

		assertNotNull(newDatabaseId);

		//test in list
		OqmMongoDatabase databaseFromDb = databaseService.getTypedCollection().find(eq("_id", newDatabaseId)).first();

		assertNotNull(databaseFromDb);
		assertEquals(newDatabaseNew, databaseFromDb);

		//test in cache
		Optional<DbCacheEntry> cacheEntry = this.databaseService.getDatabaseCache().getFromId(newDatabaseId.toHexString());

		assertTrue(cacheEntry.isPresent());
		assertEquals(newDatabaseNew, cacheEntry.get().getOqmMongoDatabase());
	}
}