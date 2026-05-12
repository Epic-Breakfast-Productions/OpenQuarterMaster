package tech.ebp.oqm.core.api.model.object.storage.items.stored.stats;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.StoredPricing;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.TotalPricing;

import javax.measure.Unit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class ItemStoredStats extends StatsWithTotalContaining {
	
	public ItemStoredStats(Unit<?> unit, Set<StoredPricing> defaultPrices) {
		super(unit);
		this.storageBlockStats = new LinkedHashMap<>();
		
		this.setPrices(
			defaultPrices.stream().map(
				p -> TotalPricing.builder()
											   .totalPrice(
												   Monetary.getDefaultAmountFactory().setCurrency(p.getFlatPrice().getCurrency()).setNumber(0).create()
											   )
											   .label(p.getLabel())
											   .asOfDate(p.getAsOfDate())
											   .build()
			).collect(Collectors.toCollection(LinkedHashSet::new))
		);
	}
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Map<ObjectId, StoredInBlockStats> storageBlockStats = new LinkedHashMap<>();
	
	@lombok.Builder.Default
	private boolean lowStock = false;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isAnyLowStock() {
		return this.lowStock || this.getNumLowStock() != 0;
	}
}
