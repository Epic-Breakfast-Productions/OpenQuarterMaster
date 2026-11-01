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
import java.util.HashMap;
import java.util.List;
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


    /**
     * Throttles the sending of alerts based on user preferences and event metrics.
     * if there is no existing bucket for the user preference, a new bucket is created and persisted.
     * if there is an existing bucket, the event count for the specific database and event type is incremented and the bucket is updated.
     * The sentTime of the bucket is set to the current time plus the throttle time specified in the user preferences.
     */
    public void throttle(UserPreferences userPreferences, EventNotificationWrapper message) {
        log.debug("Throttling alert for user {} with preference {}", userPreferences.userId, userPreferences.id);
        Optional<EventMetrics> bucket = eventMetricsRepository.findByPreferenceId(userPreferences.id.toHexString());
        if (bucket.isEmpty()) {
            EventMetrics newBucket = this.fillNewBucket(userPreferences, message);
            eventMetricsRepository.persist(newBucket);
        } else {
            EventMetrics existingBucket = bucket.get();
            if (existingBucket.getEventCountsByDatabaseId() == null) {
                existingBucket.setEventCountsByDatabaseId(new HashMap<>());
            }
            existingBucket.getEventCountsByDatabaseId()
                .computeIfAbsent(message.getDatabase(), _ -> new HashMap<>())
                .merge(message.getEventType().name(), 1, Integer::sum);
            eventMetricsRepository.update(existingBucket);
        }
    }

    private EventMetrics fillNewBucket(UserPreferences userPreferences, EventNotificationWrapper message) {
        EventMetrics bucket = new EventMetrics();
        bucket.setPreferenceId(userPreferences.id.toHexString());
        bucket.setSentTime(calculateSendAfter(userPreferences.throttleTime));
        bucket.setEventCountsByDatabaseId(new HashMap<>());
        bucket.getEventCountsByDatabaseId()
            .computeIfAbsent(message.getDatabase(), _ -> new HashMap<>())
            .put(message.getEventType().name(), 1);
        return bucket;
    }

    private Instant calculateSendAfter(Duration throttleTime) {
        return Instant.now().plus(throttleTime);
    }

    // TODO: get time from config
    @Scheduled(every = "15s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void processReadyBuckets() {
        log.debug("Processing ready alert buckets");
        for (EventMetrics eventMetrics : eventMetricsRepository.findReady(Instant.now())) {
            try {
                this.process(eventMetrics);
            } catch (Exception e) {
                log.error("Failed to process alert bucket {}", eventMetrics.id, e);
            }
        }
    }

    public void process(EventMetrics eventMetrics) {
        log.debug("Processing alert bucket {}", eventMetrics.id);
        UserPreferences userPreferences = userPreferencesService.get(eventMetrics.getPreferenceId());
        List<ObjectId> connectionDetailIds = userPreferences.connectionDetailIds;
        log.debug("process. connectionDetailIds: {}", connectionDetailIds);
        for (ConnectionDetails connectionDetails : connectionDetailsService.getByIds(connectionDetailIds)) {
            alertHandler.sendMetrics(connectionDetails, eventMetrics);
        }
        this.delete(eventMetrics);
    }

    private void delete(EventMetrics eventMetrics) {
        eventMetricsRepository.delete(eventMetrics);
    }
}