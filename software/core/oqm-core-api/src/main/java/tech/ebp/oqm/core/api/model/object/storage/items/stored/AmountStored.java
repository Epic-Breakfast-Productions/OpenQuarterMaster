package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
@SuperBuilder(toBuilder = true)
public class AmountStored extends Stored {
	
	/**
	 * The amount of the thing stored.
	 */
	@NotNull
	@NonNull
	@ValidQuantity
	private Quantity<?> amount;

	/**
	 * The threshold of low stock for the entire object.
	 * <p>
	 * Null for no threshold, Quantity with compatible unit to set the threshold.
	 */
	@lombok.Builder.Default
	private Quantity<?> lowStockThreshold = null;
	
	public AmountStored(Unit<?> unit) {
		this(Quantities.getQuantity(0, unit), null);
	}
	
	public AmountStored(Number amount, Unit<?> unit) {
		this(Quantities.getQuantity(amount, unit), null);
	}
	
	public AmountStored setAmount(Number amount, Unit<?> unit) {
		return this.setAmount(Quantities.getQuantity(amount, unit));
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	public AmountStored add(Quantity amount) {
		this.setAmount(this.getAmount().add(amount));
		return this;
	}
	
	/**
	 *
	 * @param amount
	 * @return The resulting amount stored (this)
	 * @throws NotEnoughStoredException
	 */
	public AmountStored subtract(Quantity amount) throws NotEnoughStoredException {
		@SuppressWarnings({"rawtypes", "unchecked"})
		Quantity result = this.getAmount().subtract(amount);
		
		if (result.getValue().doubleValue() < 0.0) {
			throw new NotEnoughStoredException("Resulting amount less than zero. (subtracting "+amount+" from "+this.getAmount()+" resulting in "+result+")");
		}
		this.setAmount(result);
		return this;
	}
	
	@Override
	public StoredType getType() {
		return StoredType.AMOUNT;
	}
	
	@Override
	public String getDefaultLabelFormat() {
		return "{amt}";
	}
}
