package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public abstract class StatsWithTotalContaining extends BasicStatsContaining {

	public StatsWithTotalContaining(Unit<?> unit){
		this();
		this.total = Quantities.getQuantity(0, unit);
	}

	private Quantity<?> total;
}
