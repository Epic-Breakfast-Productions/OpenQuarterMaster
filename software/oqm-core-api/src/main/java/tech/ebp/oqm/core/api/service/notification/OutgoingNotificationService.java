package tech.ebp.oqm.core.api.service.notification;

import io.smallrye.reactive.messaging.annotations.Broadcast;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import tech.ebp.oqm.core.api.model.object.history.ObjectHistoryEvent;

/**
 * Don't
 * use
 * this
 * anywhere
 * except
 * in
 * {@link
 * HistoryEventNotificationService}
 * <p>
 * Exists
 * to
 * skirt
 * around
 * CDI
 * issues
 * related
 * to
 * enabledness
 * of
 * the
 * emitter.
 */
@ApplicationScoped
public class OutgoingNotificationService {

	@Inject
	@Broadcast
	@Channel(HistoryEventNotificationService.OUTGOING_EVENT_CHANNEL)
	@OnOverflow(value = OnOverflow.Strategy.DROP)
	Emitter<EventNotificationWrapper>
		outgoingEventEmitter;
	
	public void sendEvent(
		Message<EventNotificationWrapper> notificationWrapper
	) {
		this.outgoingEventEmitter.send(
			notificationWrapper
		);
	}
	
}
