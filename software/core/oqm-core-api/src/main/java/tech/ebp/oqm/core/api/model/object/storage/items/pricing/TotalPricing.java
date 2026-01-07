package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.money.MonetaryAmount;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
/**
 * Validators TODO:
 *
 *  - MonetaryAmounts have same currency?
 *  - perUnit both null or not null
 */
public class TotalPricing extends Pricing {
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private MonetaryAmount totalPrice = null;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getTotalPriceString() {
		return format(this.getTotalPrice());
	}
	
	public synchronized TotalPricing add(CalculatedPricing pricing){
		this.setTotalPrice(
			this.getTotalPrice().add(pricing.getTotalPrice())
		);
		return this;
	}
	
}
