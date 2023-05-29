package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.storage.items.checkout.CheckoutDetail;
import tech.ebp.oqm.lib.core.object.storage.items.utils.QuantitySumHelper;
import tech.ebp.oqm.lib.core.units.OqmProvidedUnits;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.ListAmountStoredWrapper;
import tech.ebp.oqm.lib.core.validation.annotations.ValidHeldStoredUnits;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ValidHeldStoredUnits
public class ListAmountItem extends InventoryItem<AmountStored, List<AmountStored>, ListAmountStoredWrapper> {
	
	@Override
	public StorageType getStorageType() {
		return StorageType.AMOUNT_LIST;
	}
	
	@Override
	public Quantity<?> recalcTotalCheckedOut() {
		QuantitySumHelper helper = new QuantitySumHelper(this.getUnit());
		
		helper.addAll(
			this.getCheckoutList()
				.stream()
				.map(CheckoutDetail::getItem)
				.map(AmountStored::getAmount)
		);
		
		this.setTotalCheckedOut(helper.getTotal());
		return this.getTotalCheckedOut();
	}
	
	
	@Override
	protected ListAmountStoredWrapper newWrapperInstance() {
		return new ListAmountStoredWrapper().setParentUnit(this.getUnit());
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
	
	public ListAmountItem setUnit(Unit<?> unit) {
		this.unit = unit;
		this.getStorageMap().values().forEach((ListAmountStoredWrapper w)->w.setParentUnit(this.getUnit()));
		return this;
	}
	
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
