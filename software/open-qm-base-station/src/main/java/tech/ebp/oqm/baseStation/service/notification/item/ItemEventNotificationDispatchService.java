package tech.ebp.oqm.baseStation.service.notification.item;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.StoredWrapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ItemEventNotificationDispatchService extends ItemEventNotificationService<ObjectHistoryEvent> {
	
	@Inject
	ItemExpiredEventNotificationService ieens;
	@Inject
	ItemExpiryWarnEventNotificationService iewens;
	@Inject
	ItemLowStockEventNotificationService ilsens;
	
	/**
	 * TODO:: other events
	 * @param item
	 * @param event
	 * @param <S>
	 * @param <C>
	 * @param <W>
	 * @param <I>
	 */
	@WithSpan
	@Override
	public <S extends Stored, C, W extends StoredWrapper<C, S>, I extends InventoryItem<S, C, W>> void sendEvent(
		I item,
		ObjectHistoryEvent event
	) {
		Class<? extends ObjectHistoryEvent> eventClass = event.getClass();
		if(eventClass.isAssignableFrom(ItemExpiredEvent.class)) {
			this.ieens.sendEvent(item, (ItemExpiredEvent) event);
		} else if(eventClass.isAssignableFrom(ItemExpiryWarningEvent.class)){
			this.iewens.sendEvent(item, (ItemExpiryWarningEvent) event);
		} else if(eventClass.isAssignableFrom(ItemLowStockEvent.class)){
			this.ilsens.sendEvent(item, (ItemLowStockEvent) event);
		} else {
			throw new IllegalStateException("Unsupported item history given. Cannot determine which service to pass to.");
		}
	}
}
