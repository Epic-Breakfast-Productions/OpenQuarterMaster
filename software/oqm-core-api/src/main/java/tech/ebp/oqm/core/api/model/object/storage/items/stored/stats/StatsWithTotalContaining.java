package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import lombok.*;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class StatsWithTotalContaining extends BasicStatsContaining {

	public StatsWithTotalContaining(Unit<?> unit){
		this();
		this.total = Quantities.getQuantity(0, unit);
	}

	private Quantity<?> total;
}
