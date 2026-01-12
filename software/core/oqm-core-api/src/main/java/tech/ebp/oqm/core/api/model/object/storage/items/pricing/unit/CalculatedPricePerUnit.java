package tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.Pricing;

import javax.money.MonetaryAmount;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CalculatedPricePerUnit extends PricePerUnit {
	
	/**
	 * The actual price described.
	 */
	@NotNull
	@NonNull
	private MonetaryAmount totalPrice;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getTotalPriceString() {
		return Pricing.format(this.getTotalPrice());
	}
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getTotalPriceWithPerUnitString() {
		return this.getTotalPriceString() + " @ " + this.getPricePerUnitString() + "/" + this.getUnit().getSymbol();
	}
}
