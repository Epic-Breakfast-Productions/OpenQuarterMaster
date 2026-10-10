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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class InTransitStoredStats extends StatsWithTotalContaining {

	public InTransitStoredStats(Unit<?> unit, Set<StoredPricing> defaultPrices) {
		super(unit);

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

}
