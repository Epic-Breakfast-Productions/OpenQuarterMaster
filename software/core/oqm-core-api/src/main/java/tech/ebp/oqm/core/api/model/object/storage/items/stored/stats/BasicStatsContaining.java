package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class BasicStatsContaining {
	
	@lombok.Builder.Default
	private long numStored = 0;
	
	@lombok.Builder.Default
	private long numLowStock = 0;
	
	@lombok.Builder.Default
	private long numExpiryWarn = 0;
	
	@lombok.Builder.Default
	private long numExpired = 0;
}
