package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AlertUserBucketRepository implements PanacheMongoRepository<AlertUserBucket> {
    AlertUserBucket findByUserId(String userId) {
        return find("userId", userId).firstResult();
    }
}
