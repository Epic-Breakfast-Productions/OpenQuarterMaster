package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import lombok.*;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoredInBlockStats extends StatsWithTotalContaining {

	public StoredInBlockStats(Unit<?> unit){
		super(unit);
	}

	private boolean hasStored = false;
}
