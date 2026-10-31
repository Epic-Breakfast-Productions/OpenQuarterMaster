package tech.ebp.oqm.plugin.alertMessenger.scheduled;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class AlertEventIndexInitializer {

    private static final String COLLECTION_NAME = "alertEvents";
    private static final String CREATED_AT_INDEX_NAME = "alertEvents_createdAt_ttl";
    private final MongoClient mongoClient;
    private final String databaseName;

    public AlertEventIndexInitializer(MongoClient mongoClient, @ConfigProperty(name = "quarkus.mongodb.database") String databaseName) {
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
    }

    void createIndexes(@Observes StartupEvent event) {
        mongoClient
            .getDatabase(databaseName)
            .getCollection(COLLECTION_NAME)
            .createIndex(
                Indexes.ascending("createdAt"),
                new IndexOptions()
                    .name(CREATED_AT_INDEX_NAME)
                    .expireAfter(30L, TimeUnit.DAYS)
            );
    }
}
