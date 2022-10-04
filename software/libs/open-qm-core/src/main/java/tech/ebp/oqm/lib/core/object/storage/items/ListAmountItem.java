package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored.ListAmountStoredWrapper;
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


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ValidHeldStoredUnits
public class ListAmountItem extends InventoryItem<ListAmountStoredWrapper> {//TODO:: when superbuild, this annotation can't happen
	
	@Override
	public StorageType getStorageType() {
		return StorageType.AMOUNT_LIST;
	}
	
	//	@Override
	//	protected List<@NotNull AmountStored> newTInstance() {
	//		return new ArrayList<>();
	//	}
	//
	//	public ListAmountItem add(ObjectId storageId, AmountStored stored) {
	//		List<AmountStored> storageList = this.getStoredForStorage(storageId);
	//
	//		storageList.add(stored);
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
	
	//
	//	@Override
	//	public InventoryItem<List<AmountStored>> add(ObjectId storageId, List<AmountStored> toAdd, boolean storageBlockStrict) {
	//		List<AmountStored> storedList = this.getStoredForStorage(storageId, !storageBlockStrict);
	//
	//		if (storedList == null) {
	//			//TODO:: custom exception
	//			throw new IllegalArgumentException("No storage block found with that Id.");
	//		}
	//
	//		storedList.addAll(toAdd);
	//
	//		this.getStorageMap().put(storageId, storedList);
	//
	//		this.recalcTotal();
	//		return this;
	//	}
	//
	//	@Override
	//	public ListAmountItem subtract(ObjectId storageId, List<AmountStored> toSubtract) {
	//		List<AmountStored> storedList = this.getStoredForStorage(storageId, false);
	//
	//		if (storedList == null) {
	//			//TODO:: custom exception
	//			throw new IllegalArgumentException("Nothing was stored here in the first place.");
	//		}
	//
	//		List<AmountStored> subtracted = new ArrayList<>(storedList);
	//
	//		for (AmountStored curToSubtract : subtracted) {
	//			if (!subtracted.remove(curToSubtract)) {
	//				//TODO:: custom exception
	//				throw new IllegalArgumentException("Amount did not exist in storage to remove.");
	//			}
	//		}
	//
	//		this.getStorageMap().put(storageId, subtracted);
	//
	//		this.recalcTotal();
	//		return this;
	//	}
}
