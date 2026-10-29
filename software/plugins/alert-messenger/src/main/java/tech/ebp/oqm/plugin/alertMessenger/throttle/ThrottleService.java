package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertHandler;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class ThrottleService {

    @Inject
    EventMetricsRepository eventMetricsRepository;

    @Inject
    UserPreferencesService userPreferencesService;

    @Inject
    ConnectionDetailsService connectionDetailsService;

    @Inject
    AlertHandler alertHandler;


    public void throttle(UserPreferences userPreferences, EventNotificationWrapper message) {
        log.debug("Throttling alert for user {} with preference {}", userPreferences.userId, userPreferences.id);
        EventMetrics bucket = this.getOrCreateUserBucket(userPreferences);
        bucket.getEventCountsByDatabaseId().computeIfAbsent(
            message.getDatabase(),
            _ -> Map.of()).merge(
            message.getEventType(), 1, Integer::sum);

        eventMetricsRepository.persist(bucket);
    }

    private EventMetrics getOrCreateUserBucket(UserPreferences userPreferences) {
        Optional<EventMetrics> bucket = eventMetricsRepository.findByPreferenceId(userPreferences.id.toHexString());
        return bucket.orElseGet(() -> {
            EventMetrics newBucket = new EventMetrics();
            newBucket.setPreferenceId(userPreferences.id.toHexString());
            newBucket.setSentTime(calculateSendAfter(userPreferences.throttleTime));
            return newBucket;
        });
    }

    private Instant calculateSendAfter(Duration throttleTime) {
        return Instant.now().plus(throttleTime);
    }

    // TODO: get time from config
    @Scheduled(every = "15s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void processReadyBuckets() {
        for (EventMetrics eventMetrics : eventMetricsRepository.findReady(Instant.now())) {
            try {
                this.process(eventMetrics);
            } catch (Exception e) {
                log.error("Failed to process alert bucket {}", eventMetrics.id, e);
            }
        }
    }

    // TODO: check if tansactional works here
    @Transactional
    private void process(EventMetrics eventMetrics) {
        UserPreferences userPreferences = userPreferencesService.get(eventMetrics.getPreferenceId());
        List<ObjectId> connectionDetailIds = userPreferences.connectionDetailIds;
        for (ConnectionDetails connectionDetails : connectionDetailsService.getByIds(connectionDetailIds)) {
            alertHandler.sendBulkAlert(connectionDetails, eventMetrics);
        }
        this.delete(eventMetrics);
    }

    private void delete(EventMetrics eventMetrics) {
        eventMetricsRepository.delete(eventMetrics);
    }
}