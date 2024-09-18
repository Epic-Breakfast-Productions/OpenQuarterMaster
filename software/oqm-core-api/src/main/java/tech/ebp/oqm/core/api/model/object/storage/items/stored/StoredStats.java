package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.measure.Quantity;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoredStats {
	private long numStored;
	private Quantity<?> total;
	//TODO:: expired, low stock

	private Map<ObjectId, StoredInBlockStats> storageBlockStats;
}
