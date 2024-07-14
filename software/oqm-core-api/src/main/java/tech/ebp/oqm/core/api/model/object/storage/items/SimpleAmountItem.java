package tech.ebp.oqm.core.api.model.object.storage.items;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.StorageType;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidHeldStoredUnits;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUnit;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ValidHeldStoredUnits
public class SimpleAmountItem extends InventoryItem<AmountStored, AmountStored, SingleAmountStoredWrapper> {
	
	@Override
	public StorageType getStorageType() {
		return StorageType.AMOUNT_SIMPLE;
	}
	
	@Override
	protected SingleAmountStoredWrapper newWrapperInstance() {
		return new SingleAmountStoredWrapper(new AmountStored(Quantities.getQuantity(0, this.getUnit())));
	}
	
	/*
	TODO:: use the AmountItem as the superclass, once https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578 resolved.
	 */
	
	/**
	 * The unit used to measure the item.
	 */
	@NonNull
	@ValidUnit
	private Unit<?> unit = OqmProvidedUnits.UNIT;
	
	/**
	 * The value of this item per the unit set by {@link #unit}.
	 */
	@NonNull
	@DecimalMin("0.0")
	private BigDecimal valuePerUnit = BigDecimal.ZERO;
	
	@Override
	public BigDecimal recalcValueOfStored() {
		Quantity<?> total = this.recalcTotal();
		
		this.setValueOfStored(
			BigDecimal.valueOf(total.getValue().doubleValue()).multiply(this.getValuePerUnit())
		);
		
		return this.getValueOfStored();
	}
	
}
