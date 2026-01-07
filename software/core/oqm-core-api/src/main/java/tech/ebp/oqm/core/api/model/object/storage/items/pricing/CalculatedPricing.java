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
@Setter(AccessLevel.PROTECTED)
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
	private MonetaryAmount perUnitPrice = null;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getPerUnitPriceString() {
		return format(this.getPerUnitPrice());
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
			sum = sum.add(this.getPerUnitPrice());
		}
		
		return sum;
	}
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getTotalPriceString() {
		return format(this.getTotalPrice());
	}
	
}
