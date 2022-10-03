package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.ListStoredWrapper;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;

/**
 * TODO:: figure out validator to ensure unit validity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ListAmountStoredWrapper extends ListStoredWrapper<AmountStored> {
	
	@Override
	public Quantity<?> recalcTotal() {
		//TODO:: what happens when nothing in list? Need a unit from parent?
		this.setTotal(this.stream().map(AmountStored::getAmount).reduce(Quantity::add).get());
		return this.getTotal();
	}
	
	@Override
	public void addStored(AmountStored stored) {
		this.add(stored);
	}
	
	@Override
	public AmountStored subtractStored(AmountStored stored) throws NotEnoughStoredException {
		boolean result = this.remove(stored);
		if (!result) {
			throw new NotEnoughStoredException("Stored to remove was not held.");
		}
		return stored;
	}
}
