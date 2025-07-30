package tech.ebp.oqm.core.api.model.object.storage.items.notification.processing;


import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.events.item.ItemExpiryLowStockEvent;

import java.util.List;

public interface ItemProcessResults {
	List<ItemExpiryLowStockEvent> getEvents(ObjectId transactionId);
}
