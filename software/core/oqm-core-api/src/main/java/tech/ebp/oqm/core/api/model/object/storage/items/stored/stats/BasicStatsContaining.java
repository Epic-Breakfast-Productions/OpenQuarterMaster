package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicStatsContaining {

	private long numStored = 0;
	private long numLowStock = 0;
	private long numExpiryWarn = 0;
	private long numExpired = 0;
}
