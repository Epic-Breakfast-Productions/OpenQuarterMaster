package com.ebp.openQuarterMaster.lib.core.storage.items.stored;

import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidQuantity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.measure.Quantity;

/**
 * Stored object to describe an amount of stored substance.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class AmountStored extends Stored {
	
	/**
	 * The amount of the thing stored.
	 */
	@ValidQuantity
	private Quantity<?> amount = null;
}
