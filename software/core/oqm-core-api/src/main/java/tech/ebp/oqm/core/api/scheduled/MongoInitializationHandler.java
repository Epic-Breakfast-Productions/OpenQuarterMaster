package tech.ebp.oqm.core.api.scheduled;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tech.ebp.oqm.core.api.service.mongo.MongoDbAwareService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;

@Singleton
public class MongoInitializationHandler {
    @Inject
    OqmDatabaseService oqmDatabaseService;

    @Inject
    @Any
    Instance<MongoDbAwareService<?, ?, ?>> mongoServices;

    void initDb() {
        for (DbCacheEntry db : oqmDatabaseService.getDatabases()) {
            String dbName = db.getDbName();
            for (MongoDbAwareService<?, ?, ?> service : mongoServices) {
                service.initDb(dbName);
            }
        }
    }
}
