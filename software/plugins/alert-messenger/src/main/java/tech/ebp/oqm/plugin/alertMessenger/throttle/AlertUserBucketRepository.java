package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class AlertUserBucketRepository implements PanacheMongoRepository<AlertUserBucket> {
    public AlertUserBucket findByUserId(String userId) {
        return find("userId", userId).firstResult();
    }

    public List<AlertUserBucket> findReady(Instant now) {
        return find("sendAfter <= ?1", now).list();
    }
}
