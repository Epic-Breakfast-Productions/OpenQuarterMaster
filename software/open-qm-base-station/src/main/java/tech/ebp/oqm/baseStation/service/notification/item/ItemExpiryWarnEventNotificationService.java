package tech.ebp.oqm.baseStation.service.notification.item;

import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;

import javax.enterprise.context.ApplicationScoped;

@Traced
@ApplicationScoped
public class ItemExpiryWarnEventNotificationService extends ItemEventNotificationService<ItemExpiryWarningEvent> {
	
	@Override
	public <S extends Stored, C, W extends StoredWrapper<C, S>, I extends InventoryItem<S, C, W>> void sendEvent(
		I item,
		ItemExpiryWarningEvent event
	) {
		//TODO
	}
}
