package tech.ebp.oqm.core.api.scheduled;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.serviceState.InstanceMutexService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

@Singleton
@Slf4j
public class MongoDbInit {

	@Inject
	InventoryItemService inventoryItemService;
	
	@Inject
	OqmDatabaseService oqmDatabaseService;
	
	@Inject
	InstanceMutexService instanceMutexService;
	
	
	/**
	 * This was introduced in version 4.4.8 ~ May 15, 2026
	 *
	 * It was intended to ensure that all inventory items created before an upgrade have a mutex registered for them.
	 *
	 * This can be removed once all inventory items have mutexes.
	 */
	private void ensureItemMutexesExist() {
		log.info("Ensuring inventory item mutexes exist.");
		
		for(DbCacheEntry curDb : this.oqmDatabaseService.getDatabases()) {
			log.info("Ensuring inventory item mutexes exist for database: {}", curDb.getDbName());
			this.inventoryItemService.iterator(curDb.getDbId().toHexString()).forEachRemaining((item) -> {
				this.instanceMutexService.register(
					this.instanceMutexService.getMutexIdFor(curDb.getDbId().toHexString(), item)
				);
			});
			log.info("DONE Ensuring inventory item mutexes exist for database: {}", curDb.getDbName());
		}
		
		log.info("DONE Ensuring inventory item mutexes exist.");
	}
	
	
	void onStart(
		@Observes
		StartupEvent ev
	) {
		this.ensureItemMutexesExist();
		
	}
}
