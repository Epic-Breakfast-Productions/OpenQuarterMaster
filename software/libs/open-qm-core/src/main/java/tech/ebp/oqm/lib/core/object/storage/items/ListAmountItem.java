package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
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
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@ValidHeldStoredUnits
public class ListAmountItem extends InventoryItem<List<@NotNull AmountStored>> {
	
	public ListAmountItem() {
		super(StorageType.AMOUNT_LIST);
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
	
	@Override
	public BigDecimal recalcValueOfStored() {
		this.recalcTotal();
		this.setValueOfStored(this.getValuePerUnit().multiply(BigDecimal.valueOf((double) this.getTotal().getValue())));
		return this.getValueOfStored();
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
	public BigDecimal getValueOfStored() {
		Number totalNum = this.getTotal().getValue();
		
		if (totalNum instanceof Double) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		} else if (totalNum instanceof Integer) {
			return this.getValuePerUnit().multiply(BigDecimal.valueOf(totalNum.doubleValue()));
		}
		throw new UnsupportedOperationException("Implementation does not yet support: " + totalNum.getClass().getName());
	}
	
	@Override
	public InventoryItem<List<AmountStored>> add(ObjectId storageId, List<AmountStored> toAdd, boolean storageBlockStrict) {
		List<AmountStored> storedList = this.getStoredForStorage(storageId, !storageBlockStrict);
		
		if (storedList == null) {
			//TODO:: custom exception
			throw new IllegalArgumentException("No storage block found with that Id.");
		}
		
		storedList.addAll(toAdd);
		
		this.getStorageMap().put(storageId, storedList);
		
		this.recalcTotal();
		return this;
	}
	
	@Override
	public ListAmountItem subtract(ObjectId storageId, List<AmountStored> toSubtract) {
		List<AmountStored> storedList = this.getStoredForStorage(storageId, false);
		
		if (storedList == null) {
			//TODO:: custom exception
			throw new IllegalArgumentException("Nothing was stored here in the first place.");
		}
		
		List<AmountStored> subtracted = new ArrayList<>(storedList);
		
		for (AmountStored curToSubtract : subtracted) {
			if (!subtracted.remove(curToSubtract)) {
				//TODO:: custom exception
				throw new IllegalArgumentException("Amount did not exist in storage to remove.");
			}
		}
		
		this.getStorageMap().put(storageId, subtracted);
		
		this.recalcTotal();
		return this;
	}
}
