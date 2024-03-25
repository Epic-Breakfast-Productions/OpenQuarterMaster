package tech.ebp.oqm.baseStation.service.notification;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@ApplicationScoped
public class HistoryEventNotificationService {
	
	public static final String INTERNAL_EVENT_CHANNEL = "events-internal";
	public static final String OUTGOING_EVENT_CHANNEL = "events-outgoing";
	public static final String ALL_EVENT_TOPIC = "all-events";
	
	@Inject
	@Channel(INTERNAL_EVENT_CHANNEL)
	Emitter<EventNotificationWrapper> internalEventEmitter;
	
	@Inject
	@Channel(OUTGOING_EVENT_CHANNEL)
	Emitter<ObjectHistoryEvent> outgoingEventEmitter;
	
	//TODO:: add config for outgoing
	//TODO:: use in update code
	//TODO:: test and verify; ensure broadcast
	
	/**
	 * Don't call this directly, use the other one
	 */
	@Incoming(INTERNAL_EVENT_CHANNEL)
	void sendEventOutgoing(EventNotificationWrapper notificationWrapper) {
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
	}
	
	public void sendEvent(Class<?> objectClass, ObjectHistoryEvent event) {
		this.sendEvents(objectClass, event);
	}
	
	public void sendEvents(Class<?> objectClass, ObjectHistoryEvent... events) {
		this.sendEvents(objectClass, Arrays.asList(events));
	}
	
	public void sendEvents(Class<?> objectClass, Collection<ObjectHistoryEvent> events) {
		for (ObjectHistoryEvent event : events) {
			this.internalEventEmitter.send(new EventNotificationWrapper(objectClass.getSimpleName(), event));
		}
	}
	
}
