package tech.ebp.oqm.lib.core.object.storage.items.stored;

import tech.ebp.oqm.lib.core.validation.annotations.ValidQuantity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.measure.Quantity;

/**
 * Stored object to describe an amount of stored substance.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AmountStored extends Stored {
	
	public AmountStored() {
		this.setStoredType(StoredType.AMOUNT);
	}
	
	/**
	 * The amount of the thing stored.
	 */
	@ValidQuantity
	private Quantity amount = null;
	
	public AmountStored add(AmountStored amount) {
		Quantity orig = this.getAmount();
		
		if (orig != null) {
			this.setAmount(
				orig.add(amount.getAmount())
			);
		} else {
			this.setAmount(amount.getAmount());
		}
		return this;
	}
	
	public AmountStored subtract(AmountStored amount) {
		Quantity result = this.getAmount().subtract(amount.getAmount());
		
		if (result.getValue().doubleValue() < 0) {
			//TODO:: custom exception
			throw new IllegalArgumentException("Resulting amount less than zero.");
		}
		this.setAmount(result);
		return this;
	}
	
}
