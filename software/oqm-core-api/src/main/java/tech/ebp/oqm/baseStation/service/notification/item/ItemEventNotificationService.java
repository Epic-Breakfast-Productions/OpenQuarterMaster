package tech.ebp.oqm.baseStation.service.notification.item;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.StoredWrapper;
import tech.ebp.oqm.baseStation.service.notification.HistoryEventNotificationService;

import java.util.Collection;

public abstract class ItemEventNotificationService<T extends ObjectHistoryEvent> extends HistoryEventNotificationService<T> {
	
	@WithSpan
	public abstract  <S extends Stored, C, W extends StoredWrapper<C, S>, I extends InventoryItem<S, C, W>> void sendEvent(
		I item,
		T event
	);
	
	@WithSpan
	public <S extends Stored, C, W extends StoredWrapper<C, S>, I extends InventoryItem<S, C, W>> void sendEvents(
		I item,
		Collection<T> events
	){
		events.stream().forEach((T event)->{
			sendEvent(item, event);
		});
	}
}
