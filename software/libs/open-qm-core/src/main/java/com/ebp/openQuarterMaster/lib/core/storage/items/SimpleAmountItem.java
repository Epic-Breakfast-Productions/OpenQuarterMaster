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
import java.math.BigDecimal;


@EqualsAndHashCode(callSuper = true)
@Data
@ValidHeldStoredUnits
public class SimpleAmountItem extends InventoryItem<AmountStored> {
	
	public SimpleAmountItem() {
		super(StoredType.AMOUNT_SIMPLE);
	}
	
	@Override
	public Quantity<?> recalcTotal() {
		Quantity<?> total = Quantities.getQuantity(0, this.getUnit());
		for (AmountStored cur : this.getStorageMap().values()) {
			@SuppressWarnings("rawtypes")
			Quantity amount = cur.getAmount();
			if (amount == null) {
				continue;
			}
			//noinspection unchecked
			total = total.add(amount);
		}
		
		this.setTotal(total);
		return this.getTotal();
	}
	
	@Override
	public long numStored() {
		return this.getStorageMap().size();
	}
	
	@Override
	protected AmountStored newTInstance() {
		throw new UnsupportedOperationException("Don't call this for this class; no reason to need it");
	}
	
	public SimpleAmountItem add(ObjectId storageId, AmountStored stored) {
		this.getStorageMap().put(storageId, stored);
		
		this.recalcTotal();
		return this;
	}
	
	
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
	public BigDecimal valueOfStored() {
		Number totalNum = this.getTotal().getValue();
		
		if (totalNum instanceof Double) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		} else if (totalNum instanceof Integer) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		}
		throw new UnsupportedOperationException("Implementation does not yet support: " + totalNum.getClass().getName());
	}
}
