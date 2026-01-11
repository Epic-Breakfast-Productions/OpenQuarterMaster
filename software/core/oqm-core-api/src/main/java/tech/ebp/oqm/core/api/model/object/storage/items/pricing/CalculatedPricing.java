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
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit.CalculatedPricePerUnit;

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
public class CalculatedPricing extends Pricing {
	
	/**
	 * If this pricing was calculated from default values from a stored's item
	 */
	@lombok.Builder.Default
	private boolean fromDefault = false;
	
	
	/**
	 * The actual price described.
	 */
	@NonNull
	@NotNull
	private MonetaryAmount flatPrice;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getFlatPriceString() {
		return format(this.getFlatPrice());
	}
	
	/**
	 * The actual price described.
	 */
	@lombok.Builder.Default
	private CalculatedPricePerUnit perUnitPrice = null;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getPerUnitPriceString() {
		if(this.perUnitPrice == null){
			return null;
		}
		return this.getPerUnitPrice().getTotalPriceWithPerUnitString();
	}
	
	@lombok.Builder.Default
	private MonetaryAmount totalPrice = null;
	
	public MonetaryAmount getTotalPrice(){
		if(this.totalPrice == null){
			this.setTotalPrice(this.calculateTotal());
		}
		return this.totalPrice;
	}
	
	private MonetaryAmount calculateTotal(){
		MonetaryAmount sum = this.getFlatPrice();
		
		if(this.getPerUnitPrice() != null){
			sum = sum.add(this.getPerUnitPrice().getTotalPrice());
		}
		
		return sum;
	}
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getTotalPriceString() {
		return format(this.getTotalPrice());
	}
	
}
