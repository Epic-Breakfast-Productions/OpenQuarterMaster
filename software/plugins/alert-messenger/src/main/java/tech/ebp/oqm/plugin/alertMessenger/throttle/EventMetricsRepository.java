package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EventMetricsRepository implements PanacheMongoRepository<EventMetrics> {
    List<EventMetrics> findReady(Instant time) {
        return find("sentTime <= ?1", time).list();
    }

    Optional<EventMetrics> findByPreferenceId(String preferenceId) {
        return find("preferenceId", preferenceId).firstResultOptional();
    }
}
