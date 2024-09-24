package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import lombok.*;
import org.bson.types.ObjectId;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ItemStoredStats extends StatsWithTotalContaining {

	public ItemStoredStats(Unit<?> unit){
		super(unit);
	}

	private Map<ObjectId, StoredInBlockStats> storageBlockStats = new LinkedHashMap<>();
}
