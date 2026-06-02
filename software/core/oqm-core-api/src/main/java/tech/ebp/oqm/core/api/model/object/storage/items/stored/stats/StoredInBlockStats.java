package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.measure.Unit;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class StoredInBlockStats extends StatsWithTotalContaining {

	public StoredInBlockStats(Unit<?> unit){
		super(unit);
	}

	@lombok.Builder.Default
	private boolean lowStock = false;

	@lombok.Builder.Default
	private boolean hasStored = false;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isAnyLowStock() {
		return this.lowStock || this.isHasLowStockStored();
	}
}
