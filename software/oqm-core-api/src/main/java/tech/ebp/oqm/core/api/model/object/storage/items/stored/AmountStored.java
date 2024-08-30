package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidQuantity;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

/**
 * Stored object to describe an amount of stored substance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AmountStored extends Stored {
	
	/**
	 * The amount of the thing stored.
	 */
	@ValidQuantity
	private Quantity<?> amount = null;
	
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
		@SuppressWarnings("rawtypes")
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
	
	/**
	 *
	 * @param amount
	 * @return The resulting amount stored (this)
	 * @throws NotEnoughStoredException
	 */
	public AmountStored subtract(AmountStored amount) throws NotEnoughStoredException {
		@SuppressWarnings("rawtypes")
		Quantity otherAmount = amount.getAmount();
		@SuppressWarnings("rawtypes")
		Quantity result = this.getAmount().subtract(otherAmount);
		
		if (result.getValue().doubleValue() < 0) {
			throw new NotEnoughStoredException("Resulting amount less than zero. (subtracting "+amount.getAmount()+" from "+this.getAmount()+" resulting in "+result+")");
		}
		this.setAmount(result);
		return this;
	}
	
	@Override
	public StoredType getStoredType() {
		return StoredType.AMOUNT;
	}
	
	@Override
	public String getLabelText() {
		StringBuilder sb = new StringBuilder(this.getAmount().toString());
		
		//TODO:: add more attributes
		
		return sb.toString();
	}

	@Override
	public int getSchemaVersion() {
		return 1;
	}
}
