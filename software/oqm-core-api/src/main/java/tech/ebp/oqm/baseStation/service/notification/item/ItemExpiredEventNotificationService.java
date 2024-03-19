package tech.ebp.oqm.baseStation.service.notification.item;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.baseStation.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.StoredWrapper;

@ApplicationScoped
public class ItemExpiredEventNotificationService extends ItemEventNotificationService<ItemExpiredEvent> {
	
	@WithSpan
	@Override
	public <S extends Stored, C, W extends StoredWrapper<C, S>, I extends InventoryItem<S, C, W>> void sendEvent(
		I item,
		ItemExpiredEvent event
	) {
		//TODO
	}
}
