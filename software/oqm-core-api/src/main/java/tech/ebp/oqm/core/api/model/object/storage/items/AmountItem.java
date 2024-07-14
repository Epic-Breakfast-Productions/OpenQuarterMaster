package tech.ebp.oqm.core.api.model.object.storage.items;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.StoredWrapper;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidHeldStoredUnits;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUnit;

import javax.measure.Unit;
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
public abstract class AmountItem<C, T extends StoredWrapper<C, AmountStored>> extends InventoryItem<AmountStored, C, T> {
	
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
