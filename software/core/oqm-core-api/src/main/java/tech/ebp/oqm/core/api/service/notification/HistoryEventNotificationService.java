package tech.ebp.oqm.core.api.service.notification;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class HistoryEventNotificationService {

	public static final String INTERNAL_EVENT_CHANNEL = "events-internal";
	public static final String OUTGOING_EVENT_CHANNEL = "events-outgoing";
	public static final String TOPIC_PREPEND = "oqm-core-";
	public static final String ALL_EVENT_TOPIC_LABEL = "all-events";
	public static final String ALL_EVENT_TOPIC = TOPIC_PREPEND + ALL_EVENT_TOPIC_LABEL;
	
	@ConfigProperty(name = "mp.messaging.outgoing.events-outgoing.enabled", defaultValue = "false")
	Boolean outgoingServersEnabled;
	
	@ConfigProperty(name = "mp.messaging.outgoing.events-outgoing.bootstrap.servers")
	Optional<String> outgoingServers;
	@ConfigProperty(name = "kafka.bootstrap.servers")
	Optional<String> kafkaServers;

	@Inject
	@Broadcast
	@Channel(INTERNAL_EVENT_CHANNEL)
	@OnOverflow(value = OnOverflow.Strategy.DROP)
	Emitter<EventNotificationWrapper> internalEventEmitter;

	@Inject
	OutgoingNotificationService outgoingEventService;

	private boolean outgoingEnabled() {
		return this.outgoingServersEnabled && (this.outgoingServers.isPresent() || this.kafkaServers.isPresent());
	}

	/**
	 * Don't call this directly, use the other one(s)
	 */
	@WithSpan
	@Incoming(INTERNAL_EVENT_CHANNEL)
	void sendEventOutgoing(EventNotificationWrapper notificationWrapper) {
		if (!this.outgoingEnabled()) {
			log.info("NOT Sending event to external channels (no outgoing servers configured): {}/{}", notificationWrapper.getClass().getSimpleName(),
				notificationWrapper.getEvent().getId());
			return;
		}
		log.info("Sending event to external channels: {}/{}", notificationWrapper.getClass().getSimpleName(), notificationWrapper.getEvent().getId());
		try {
			Headers headers = new RecordHeaders()
				.add("database", notificationWrapper.getDatabase().toHexString().getBytes())
				.add("object", notificationWrapper.getObjectName().getBytes());
			this.outgoingEventService.sendEvent(
				Message.of(notificationWrapper)
					.addMetadata(
						OutgoingKafkaRecordMetadata.<String>builder()
							.withTopic(ALL_EVENT_TOPIC)
							.withHeaders(headers)
							.build()
					));
			//TODO:: maybe support in future
//			this.outgoingEventEmitter.send(
//				Message.of(
//					notificationWrapper
//				).addMetadata(
//					OutgoingKafkaRecordMetadata.<String>builder()
//						.withTopic(
//							TOPIC_PREPEND + (notificationWrapper.getDatabase() == null ? "" : notificationWrapper.getDatabase().toHexString() + "-") + ALL_EVENT_TOPIC_LABEL
//						)
//						.withHeaders(headers)
//						.build()
//				));


			//TODO:: maybe support this in future
//			this.outgoingEventEmitter.send(
//				Message.of(notificationWrapper.getEvent()).addMetadata(
//					OutgoingKafkaRecordMetadata.<String>builder()
//						.withTopic(
//							TOPIC_PREPEND + (notificationWrapper.getDatabase() == null ? "" : notificationWrapper.getDatabase().toHexString() + "-") + notificationWrapper.getObjectName() + "-" + notificationWrapper.getEvent().getType()
//						)
//						.withHeaders(headers)
//						.build()
//				));
			log.debug("Sent event to external channels: {}/{}", notificationWrapper.getClass().getSimpleName(), notificationWrapper.getEvent().getId());
		} catch (Throwable e) {
			log.error("FAILED to send event to external channels: {}/{}:", notificationWrapper.getClass().getSimpleName(), notificationWrapper.getEvent().getId(), e);
			throw e;
		}
	}

	public void sendEvent(ObjectId oqmDatabase, Class<?> objectClass, ObjectHistoryEvent event) {
		this.sendEvents(oqmDatabase, objectClass, event);
	}

	public void sendEvents(ObjectId oqmDatabase, Class<?> objectClass, ObjectHistoryEvent... events) {
		this.sendEvents(oqmDatabase, objectClass, Arrays.asList(events));
	}

	public void sendEvents(ObjectId oqmDatabase, Class<?> objectClass, Collection<ObjectHistoryEvent> events) {
		for (ObjectHistoryEvent event : events) {
			log.info("Sending event to internal channel: {}/{}", objectClass.getSimpleName(), event.getId());
			if (event.getId() == null) {
				throw new NullPointerException("Null ID for " + event.getType() + " event given for object of type " + objectClass.getSimpleName());
			}
			this.internalEventEmitter.send(new EventNotificationWrapper(oqmDatabase, objectClass.getSimpleName(), event));
		}
	}

}
