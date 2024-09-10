package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.Builder;
import lombok.Data;

import javax.measure.Quantity;

@Data
@Builder
public class StoredInBlockStats {
	private long numStored;
	private Quantity<?> total;

	//TODO:: expired, low stock
}
