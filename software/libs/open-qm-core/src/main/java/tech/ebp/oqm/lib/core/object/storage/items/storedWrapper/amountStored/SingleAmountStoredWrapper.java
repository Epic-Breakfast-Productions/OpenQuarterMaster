package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.amountStored;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.SingleStoredWrapper;

import javax.measure.Quantity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SingleAmountStoredWrapper extends SingleStoredWrapper<AmountStored> {
	
	public SingleAmountStoredWrapper() {
		super(new AmountStored());
	}
	
	@Override
	public Quantity<?> recalcTotal() {
		this.setTotal(this.getStored().getAmount());
		return this.getTotal();
	}
	
	@Override
	public void addStored(AmountStored stored) {
		this.getStored().add(stored);
	}
	
	@Override
	public AmountStored subtractStored(AmountStored stored) throws NotEnoughStoredException {
		return this.getStored().subtract(stored);
	}
}
