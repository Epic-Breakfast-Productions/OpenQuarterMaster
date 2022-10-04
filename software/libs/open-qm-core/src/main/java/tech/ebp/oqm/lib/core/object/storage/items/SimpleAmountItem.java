package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.SingleStoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.SingleAmountStoredWrapper;
import tech.ebp.oqm.lib.core.validation.annotations.ValidHeldStoredUnits;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.DecimalMin;
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
	protected SingleAmountStoredWrapper newTInstance() {
		return new SingleAmountStoredWrapper();
	}
	
	
	//
	//	@Override
	//	protected AmountStored newTInstance() {
	//		throw new UnsupportedOperationException("Don't call this for this class; no reason to need it");
	//	}
	
	//	public SimpleAmountItem addNewStored(ObjectId storageId, AmountStored stored) {
	//		this.getStorageMap().put(storageId, stored);
	//
	//		this.recalcTotal();
	//		return this;
	//	}
	
	
	/*
	TODO:: use the AmountItem as the superclass, once https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578 resolved.
	 */
	
	/**
	 * The unit used to measure the item.
	 */
	@NonNull
	@ValidUnit
	private Unit<?> unit = UnitUtils.UNIT;
	
	/**
	 * The value of this item per the unit set by {@link #unit}.
	 */
	@NonNull
	@DecimalMin("0.0")
	private BigDecimal valuePerUnit = BigDecimal.ZERO;
	
	@Override
	public BigDecimal getValueOfStored() {
		Number totalNum = this.getTotal().getValue();
		
		if (totalNum instanceof Double) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		} else if (totalNum instanceof Integer) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		}
		throw new UnsupportedOperationException("Implementation does not yet support: " + totalNum.getClass().getName());
	}
	
	
	//
	//	@Override
	//	public InventoryItem<AmountStored> add(@NonNull ObjectId storageId, AmountStored toAdd, boolean storageBlockStrict) {
	//		AmountStored stored = this.getStoredForStorage(storageId, !storageBlockStrict);
	//
	//		if (stored == null) {
	//			//TODO:: custom exception
	//			throw new IllegalArgumentException("No storage block found with that Id.");
	//		}
	//		stored.add(toAdd);
	//
	//		this.getStorageMap().put(storageId, stored);
	//
	//		this.recalcTotal();
	//		return this;
	//	}
	//
	//	@Override
	//	public InventoryItem<AmountStored> subtract(ObjectId storageId, AmountStored toSubtract) {
	//		AmountStored stored = this.getStoredForStorage(storageId, false);
	//
	//		if (stored == null) {
	//			//TODO:: custom exception
	//			throw new IllegalArgumentException("Nothing was stored here in the first place.");
	//		}
	//
	//		stored.subtract(toSubtract);
	//		this.getStorageMap().put(storageId, stored);
	//
	//		this.recalcTotal();
	//		return this;
	//	}
}
