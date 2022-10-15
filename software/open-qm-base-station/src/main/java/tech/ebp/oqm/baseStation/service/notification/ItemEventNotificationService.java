package tech.ebp.oqm.baseStation.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.lib.core.object.history.events.HistoryEvent;
import tech.ebp.oqm.lib.core.object.storage.items.InventoryItem;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;

import javax.enterprise.context.ApplicationScoped;

@Traced
@Slf4j
@ApplicationScoped
public class ItemEventNotificationService {
	
	
	public <S extends Stored, C, W extends StoredWrapper<C, S>, T extends InventoryItem<S, C, W>>
	void sendEvent(
		T item,
		HistoryEvent event
	){
		//TODO
	}

}
