package tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.Pricing;

import javax.measure.Unit;
import javax.money.MonetaryAmount;

@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class PricePerUnit {
	
	@NonNull
	@NotNull
	private MonetaryAmount price;
	
	@NonNull
	@NotNull
	@SuppressWarnings("rawtypes")
	private Unit unit;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getPricePerUnitString() {
		return Pricing.format(this.getPrice());
	}
}
