package tech.ebp.oqm.core.api.model.object.storage.items.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit.CalculatedPricePerUnit;
import tech.ebp.oqm.core.api.model.object.storage.items.pricing.unit.PricePerUnit;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.money.MonetaryAmount;

/**
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StoredPricing extends Pricing {
	
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
	 * The price per unit. Added to the given flat pricing.
	 */
	private PricePerUnit pricePerUnit;
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public boolean isHasPricePerUnit(){
		return this.getPricePerUnit() != null;
	}
	
	//TODO:: price modifier based on condition %
	
	
	protected MonetaryAmount calcPriceFromUnit(Quantity<?> stored){
		Quantity<?> calcStored = stored;
		
		//normalize quantity to expected unit
		if(!calcStored.getUnit().equals(this.getPricePerUnit().getUnit())){
			//noinspection unchecked
			calcStored = calcStored.to(this.getPricePerUnit().getUnit());
		}
		
		return this.getPricePerUnit().getPrice().multiply(calcStored.getValue());
	}
	
	protected MonetaryAmount calcPriceFromUnit(Stored stored){
		if(stored instanceof AmountStored){
			return this.calcPriceFromUnit(((AmountStored) stored).getAmount());
		} else {
			return this.calcPriceFromUnit(UnitUtils.Quantities.UNIT_ONE);
		}
	}
	
	public CalculatedPricing calculatePrice(Stored stored){
		CalculatedPricing.CalculatedPricingBuilder<?,?> output = CalculatedPricing.builder()
													.label(this.getLabel());
		
		output.flatPrice(this.getFlatPrice());
		
		if(isHasPricePerUnit()){
			output = output.perUnitPrice(
				CalculatedPricePerUnit.builder()
					.price(this.getPricePerUnit().getPrice())
					.unit(this.getPricePerUnit().getUnit())
					.totalPrice(this.calcPriceFromUnit(stored))
					.build()
			);
		}
		
		return output.build();
	}
	
}
