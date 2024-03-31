package tech.ebp.oqm.baseStation.service.notification;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class HistoryEventNotificationService {
	
	public static final String INTERNAL_EVENT_CHANNEL = "events-internal";
	public static final String OUTGOING_EVENT_CHANNEL = "events-outgoing";
	public static final String ALL_EVENT_TOPIC = "all-events";
	
	@ConfigProperty(name = "mp.messaging.outgoing.events-outgoing.bootstrap.servers")
	Optional<String> outgoingServers;
	@ConfigProperty(name = "kafka.bootstrap.servers")
	Optional<String> kafkaServers;
	
	@Inject
	@Broadcast
	@Channel(INTERNAL_EVENT_CHANNEL)
	@OnOverflow(value = OnOverflow.Strategy.DROP)//TODO:: this better https://quarkus.io/version/3.2/guides/kafka#sending-messages-with-emitter
	Emitter<EventNotificationWrapper> internalEventEmitter;
	
	@Inject
	@Broadcast
	@Channel(OUTGOING_EVENT_CHANNEL)
	@OnOverflow(value = OnOverflow.Strategy.DROP)
	Emitter<ObjectHistoryEvent> outgoingEventEmitter;
	
	private boolean haveOutgoingServers(){
		return outgoingServers.isPresent() || kafkaServers.isPresent();
	}
	
	/**
	 * Don't call this directly, use the other one
	 */
	@WithSpan
	@Incoming(INTERNAL_EVENT_CHANNEL)
	void sendEventOutgoing(EventNotificationWrapper notificationWrapper) {
		if(!this.haveOutgoingServers()){
			log.info("NOT Sending event to external channels (no outgoing servers configured): {}/{}", notificationWrapper.getClass().getSimpleName(),
				notificationWrapper.getEvent().getId());
			return;
		}
		log.info("Sending event to external channels: {}/{}", notificationWrapper.getClass().getSimpleName(), notificationWrapper.getEvent().getId());
		try {
			this.outgoingEventEmitter.send(
				Message.of(
					notificationWrapper.getEvent()
				).addMetadata(
					OutgoingKafkaRecordMetadata.<String>builder()
						.withTopic(notificationWrapper.getObjectName() + "-" + notificationWrapper.getEvent().getType())
						.build()
				));
			this.outgoingEventEmitter.send(
				Message.of(
					notificationWrapper.getEvent()
				).addMetadata(
					OutgoingKafkaRecordMetadata.<String>builder()
						.withTopic(ALL_EVENT_TOPIC)
						.build()
				));
			log.debug("Sent event to external channels: {}/{}", notificationWrapper.getClass().getSimpleName(), notificationWrapper.getEvent().getId());
		} catch(Throwable e) {
			log.error("FAILED to send event to external channels: {}/{}:", notificationWrapper.getClass().getSimpleName(), notificationWrapper.getEvent().getId(), e);
			throw e;
		}
	}
	
	public void sendEvent(Class<?> objectClass, ObjectHistoryEvent event) {
		this.sendEvents(objectClass, event);
	}
	
	public void sendEvents(Class<?> objectClass, ObjectHistoryEvent... events) {
		this.sendEvents(objectClass, Arrays.asList(events));
	}
	
	public void sendEvents(Class<?> objectClass, Collection<ObjectHistoryEvent> events) {
		for (ObjectHistoryEvent event : events) {
			log.info("Sending event to internal channel: {}/{}", objectClass.getSimpleName(), event.getId());
			if (event.getId() == null) {
				throw new NullPointerException("Null ID for " + event.getType() + " event given for object of type " + objectClass.getSimpleName());
			}
			this.internalEventEmitter.send(new EventNotificationWrapper(objectClass.getSimpleName(), event));
		}
	}
	
}
