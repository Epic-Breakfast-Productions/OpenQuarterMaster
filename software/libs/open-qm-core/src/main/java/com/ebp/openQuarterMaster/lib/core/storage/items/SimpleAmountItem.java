package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidHeldStoredUnits;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@BsonDiscriminator(value = "AMOUNT_SIMPLE")
public class SimpleAmountItem extends AmountItem<@NotNull AmountStored> {
	
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
}
