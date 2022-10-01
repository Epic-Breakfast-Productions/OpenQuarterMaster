package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.ToString;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import tech.ebp.oqm.lib.core.validation.annotations.ValidHeldStoredUnits;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.measure.Unit;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Describes an inventory item that uses amount measurements.
 *
 * Currently unused; see https://jira.mongodb.org/projects/JAVA/issues/JAVA-4578
 * Waiting on that same jira ticket to use this class.
 *
 * @param <T>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@ValidHeldStoredUnits
public abstract class AmountItem<T> extends InventoryItem<T> {
	
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
}
