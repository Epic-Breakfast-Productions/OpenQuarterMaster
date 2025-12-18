package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.Pricing;
import tech.ebp.oqm.core.api.model.validation.annotations.UniqueLabeledCollection;

import java.util.LinkedHashSet;

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
	
	@lombok.Builder.Default
	private LinkedHashSet<@NotNull Pricing> prices = new LinkedHashSet<>();
}
