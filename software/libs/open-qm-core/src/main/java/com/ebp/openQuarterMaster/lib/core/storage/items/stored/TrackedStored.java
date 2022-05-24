package com.ebp.openQuarterMaster.lib.core.storage.items.stored;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Stored object to describe an individual, tracked item.
 * <p>
 * The key of this object in the {@link com.ebp.openQuarterMaster.lib.core.storage.items.TrackedItem} object is the identifying key for this
 * object.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TrackedStored extends Stored {
	
	/**
	 * Some extra details to help identify this exact item.
	 */
	private String identifyingDetails = "";
	
	/**
	 * The value of this particular tracked item.
	 * <p>
	 * If not set (null), will default to the default value held in the item during calculations.
	 */
	@DecimalMin("0.0")
	private BigDecimal value = null;
}
