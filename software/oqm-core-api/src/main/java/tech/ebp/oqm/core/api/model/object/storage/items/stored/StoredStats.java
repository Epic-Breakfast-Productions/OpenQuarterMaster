package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.measure.Quantity;
import java.util.Map;

@Data
@Builder
public class StoredStats {
	private long numStored;
	private Quantity<?> total;
	//TODO:: expired, low stock

	private Map<ObjectId, StoredInBlockStats> storageBlockStats;
}
