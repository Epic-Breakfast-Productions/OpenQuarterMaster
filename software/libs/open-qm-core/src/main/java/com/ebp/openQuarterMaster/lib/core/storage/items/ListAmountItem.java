package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.storage.items.stored.AmountStored;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class ListAmountItem extends AmountItem<List<@NotNull AmountStored>> {
	
	public ListAmountItem() {
		super(StoredType.AMOUNT_LIST);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Quantity<?> recalcTotal() {
		var ref = new Object() {
			Quantity<?> total = Quantities.getQuantity(0, getUnit());
			
			public synchronized void addToTotal(Quantity quantity) {
				this.total = this.total.add(quantity);
			}
		};
		
		this.getStorageMap().values().parallelStream()
			.map((storedList)->{
				Quantity curTotal = Quantities.getQuantity(0, this.getUnit());
				for (AmountStored amtStored : storedList) {
					Quantity amount = amtStored.getAmount();
					if (amount == null) {
						continue;
					}
					curTotal = curTotal.add(amount);
				}
				return curTotal;
			})
			.forEach(ref::addToTotal);
		
		this.setTotal(ref.total);
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
	protected List<@NotNull AmountStored> newTInstance() {
		return new ArrayList<>();
	}
	
	public ListAmountItem add(ObjectId storageId, AmountStored stored) {
		List<AmountStored> storageList = this.getStoredForStorage(storageId);
		
		storageList.add(stored);
		
		this.recalcTotal();
		return this;
	}
}
