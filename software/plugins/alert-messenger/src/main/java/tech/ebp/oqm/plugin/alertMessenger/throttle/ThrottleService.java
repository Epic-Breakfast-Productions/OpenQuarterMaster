package tech.ebp.oqm.plugin.alertMessenger.throttle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.messaging.EventNotificationWrapper;
import tech.ebp.oqm.plugin.alertMessenger.preferences.UserPreferences;

import java.time.Instant;

@Slf4j
@ApplicationScoped
public class ThrottleService {

    @Inject
    AlertEventRepository alertEventRepository;

    @Inject
    AlertUserBucketRepository alertUserBucketRepository;

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
    //TODO: add scheduler and delete buckets after sending messages (compact all events into one message)
}
