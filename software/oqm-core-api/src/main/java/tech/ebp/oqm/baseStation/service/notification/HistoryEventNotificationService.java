package tech.ebp.oqm.baseStation.service.notification;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;

@ApplicationScoped
public class HistoryEventNotificationService {
	public static final String EVENT_CHANNEL = "events";
	public static final String EVENT_TOPIC = "all-events";
	
	@Inject
	@Channel(EVENT_CHANNEL)
	Emitter<ObjectHistoryEvent> eventEmitter;
	
	public void sendEvent(Class<?> objectClass, ObjectHistoryEvent event){
		this.eventEmitter.send(Message.of(event).addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
																 .withTopic(objectClass.getSimpleName() + "-"+event.getType())
																 .build()));
		this.eventEmitter.send(Message.of(event).addMetadata(OutgoingKafkaRecordMetadata.<String>builder()
																 .withTopic(EVENT_TOPIC)
																 .build()));
		
	}
	
}
