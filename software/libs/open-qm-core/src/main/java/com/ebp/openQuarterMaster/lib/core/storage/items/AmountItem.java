package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidHeldStoredUnits;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidUnit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@ValidHeldStoredUnits
public class AmountItem extends InventoryItem<List<@NotNull AmountStored>> {
	
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
	
	public AmountItem() {
		super(StoredType.AMOUNT);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Quantity<?> recalcTotal() {
		//TODO:: try parallel streams
		Quantity<?> total = Quantities.getQuantity(0, this.getUnit());
		for (List<AmountStored> storedList : this.getStorageMap().values()) {
			for (AmountStored amtStored : storedList) {
				Quantity amount = amtStored.getAmount();
				if (amount == null) {
					continue;
				}
				total = total.add(amount);
			}
		}
		this.setTotal(total);
		return this.getTotal();
	}
	
	@Override
	public long numStored() {
		return this.getStorageMap()
				   .values()
				   .parallelStream()
				   .mapToLong(List::size)
				   .sum();
	}
	
	@Override
	public BigDecimal valueOfStored() {
		Number totalNum = this.getTotal().getValue();
		
		if (totalNum instanceof Double) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		} else if (totalNum instanceof Integer) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		}
		throw new UnsupportedOperationException("Implementation does not yet support: " + totalNum.getClass().getName());
	}
	
	@Override
	protected List<@NotNull AmountStored> newTInstance() {
		return new ArrayList<>();
	}
	
	public AmountItem add(ObjectId storageId, AmountStored stored) {
		List<AmountStored> storageList = this.getStoredForStorage(storageId);
		
		storageList.add(stored);
		
		this.recalcTotal();
		return this;
	}
}
