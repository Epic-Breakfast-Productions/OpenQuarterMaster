package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import javax.measure.Quantity;

public interface AmountContaining {
	Quantity<?> getAmount();
}
