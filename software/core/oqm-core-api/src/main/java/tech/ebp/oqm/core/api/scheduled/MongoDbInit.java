package tech.ebp.oqm.core.api.scheduled;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.api.model.object.upgrade.TotalUpgradeResult;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.MongoService;
import tech.ebp.oqm.core.api.service.schemaVersioning.ObjectSchemaUpgradeService;
import tech.ebp.oqm.core.api.service.serviceState.InstanceMutexService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.health.utils.HealthStatus;
import tech.ebp.oqm.core.api.health.utils.HasReadinessCheck;

import java.util.Optional;

@Singleton
@Slf4j
public class MongoDbInit implements HasReadinessCheck {

    @Inject
    InventoryItemService inventoryItemService;

    @Inject
    OqmDatabaseService oqmDatabaseService;

    @Inject
    InstanceMutexService instanceMutexService;

    @Inject
    @Any
    Instance<MongoService<?, ?, ?>> mongoServices;

	@Inject
	ObjectSchemaUpgradeService objectSchemaUpgradeService;

    /**
     * This was introduced in version 4.4.8 ~ May 15, 2026
     * <p>
     * It was intended to ensure that all inventory items created before an upgrade have a mutex registered for them.
     * <p>
     * This can be removed once all inventory items have mutexes.
     */
    private void ensureItemMutexesExist() {
        try {
            log.info("Ensuring inventory item mutexes exist.");

            for (DbCacheEntry curDb : this.oqmDatabaseService.getDatabases()) {
                log.info("Ensuring inventory item mutexes exist for database: {}", curDb.getDbName());
                this.inventoryItemService.iterator(curDb.getDbId().toHexString()).forEachRemaining((item) -> {
                    this.instanceMutexService.register(this.instanceMutexService.getMutexIdFor(curDb.getDbId().toHexString(), item));
                });
                log.info("DONE Ensuring inventory item mutexes exist for database: {}", curDb.getDbName());
            }

            log.info("DONE Ensuring inventory item mutexes exist.");
        } catch (RuntimeException e) {
            readinessStatus.markDown("Inventory item mutex initialization failed: " + e.getMessage());
            throw e;
        }
    }

	private void upgradeDbs(){
		//TODO:: create flag service to check if things initted right. Setup filter to check this flag to reject requests until setup done.
		//TODO:: integrate into healthcheck. only DOWN if db upgrade failed
		try {
			Optional<TotalUpgradeResult> schemaUpgradeResult = this.objectSchemaUpgradeService.updateSchema();
			if(schemaUpgradeResult.isEmpty()){
				log.warn("Did not upgrade schema at start.");
			} else {
				log.info("Schema upgrade result: {}", schemaUpgradeResult.get());
				//TODO:: rescan inv update stats
			}
		} catch (RuntimeException e) {
			readinessStatus.markDown("Database schema upgrade failed: " + e.getMessage());
			throw e;
		}
	}

	private void initDbs(){
		try {
			log.info("Initializing all databases.");
			for(MongoService<?, ?, ?> service : this.mongoServices){
				service.initDb();
			}
			log.info("DONE initializing all databases.");
		} catch (RuntimeException e) {
			readinessStatus.markDown("Database initialization failed: " + e.getMessage());
			throw e;
		}
	}


    @Getter
    private final HealthStatus readinessStatus = new HealthStatus("Mongo DB Init");

    void onStart(@Observes StartupEvent ev) {
        readinessStatus.markDown("Startup initialization in progress");
        this.upgradeDbs();
        this.initDbs();
        this.ensureItemMutexesExist();
        readinessStatus.markUp("Initial db initialization tasks finished");
        log.info("FINISHED initial db initialization tasks.");
    }
}
