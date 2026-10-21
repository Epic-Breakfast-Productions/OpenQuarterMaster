package tech.ebp.oqm.plugin.alertMessenger.throttle;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.alerts.AlertHandler;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetails;
import tech.ebp.oqm.plugin.alertMessenger.connections.ConnectionDetailsService;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferencesService;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

@Slf4j
@ApplicationScoped
public class ThrottleService {

    @Inject
    AlertEventRepository alertEventRepository;

    @Inject
    AlertUserBucketRepository alertUserBucketRepository;

    @Inject
    UserPreferencesService userPreferencesService;

    @Inject
    ConnectionDetailsService connectionDetailsService;

    @Inject
    AlertHandler alertHandler;

    public ObjectId saveAlertEvent(EventNotificationWrapper notification) {
        AlertEvent alertEvent = new AlertEvent(notification);
        alertEventRepository.persist(alertEvent);
        return alertEvent.id;
    }

    public void throttle(UserPreferences userPreferences, ObjectId alertEventId) {
        log.debug("Throttling alert for user {} with event ID {}", userPreferences.userId, alertEventId);
        AlertUserBucket bucket = this.getOrCreateUserBucket(userPreferences);
        bucket.getAlertEventId().add(alertEventId);
        alertUserBucketRepository.persist(bucket);
    }

    private AlertUserBucket getOrCreateUserBucket(UserPreferences userPreferences) {
        AlertUserBucket bucket = alertUserBucketRepository.findByUserId(userPreferences.userId);
        if (bucket == null) {
            bucket = new AlertUserBucket();
            bucket.setUserId(userPreferences.userId);
            bucket.setSendAfter(this.calculateSendAfter(userPreferences.throttleTimeInMinutes));
            alertUserBucketRepository.persist(bucket);
        }
        return bucket;
    }

    private Instant calculateSendAfter(int throttleTimeInMinutes) {
        return Instant.now().plusSeconds(throttleTimeInMinutes * 60L);
    }


    @Scheduled(every = "60s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void processReadyBuckets() {
        for (AlertUserBucket bucket : alertUserBucketRepository.findReady(Instant.now())) {
            try {
                process(bucket);
            } catch (Exception e) {
                log.error("Failed to process alert bucket {}", bucket.id, e);
            }
        }
    }

    private void process(AlertUserBucket bucket) {
        Iterator<UserPreferences> preferences = userPreferencesService.getForUser(bucket.getUserId());
        List<EventNotificationWrapper> events = bucket.getAlertEventId().stream()
            .map(alertEventRepository::findById)
            .map(AlertEvent::getEventNotificationWrapper).toList();

        while (preferences.hasNext()) {
            UserPreferences preference = preferences.next();
            List<ObjectId> connectionIds = preference.connectionDetailIds;
            for (ConnectionDetails connectionDetails : connectionDetailsService.getByIds(connectionIds)) {
                alertHandler.sendAlert(connectionDetails, events);
            }
        }
        this.delete(bucket);
    }

    private void delete(AlertUserBucket bucket) {
        alertUserBucketRepository.delete(bucket);
        bucket.getAlertEventId().forEach(alertEventRepository::deleteById);
    }
}
