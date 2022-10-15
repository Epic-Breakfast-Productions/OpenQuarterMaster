package tech.ebp.oqm.baseStation.service.notification;

import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;

@Traced
public abstract class HistoryEventNotificationService<T extends HistoryEvent> {

}
