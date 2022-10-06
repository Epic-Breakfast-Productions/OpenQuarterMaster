package tech.ebp.oqm.lib.core.object.storage.items.stored;

import lombok.AllArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.validation.annotations.ValidQuantity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

/**
 * Stored object to describe an amount of stored substance.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AmountStored extends Stored {
	
	/**
	 * The amount of the thing stored.
	 */
	@ValidQuantity
	private Quantity amount = null;
	
	public AmountStored(Unit<?> unit) {
		this(Quantities.getQuantity(0, unit));
	}
	
	public AmountStored(Number amount, Unit<?> unit) {
		this(Quantities.getQuantity(amount, unit));
	}
	
	public AmountStored setAmount(Number amount, Unit<?> unit) {
		return this.setAmount(Quantities.getQuantity(amount, unit));
	}
	
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
	
	public AmountStored subtract(AmountStored amount) throws NotEnoughStoredException {
		Quantity result = this.getAmount().subtract(amount.getAmount());
		
		if (result.getValue().doubleValue() < 0) {
			throw new NotEnoughStoredException("Resulting amount less than zero.");
		}
		this.setAmount(result);
		return this;
	}
	
	@Override
	public StoredType getStoredType() {
		return StoredType.AMOUNT;
	}
}
