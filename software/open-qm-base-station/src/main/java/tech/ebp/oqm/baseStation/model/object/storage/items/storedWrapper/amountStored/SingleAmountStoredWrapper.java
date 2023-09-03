package tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.amountStored;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.SingleStoredWrapper;

import javax.measure.Quantity;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SingleAmountStoredWrapper extends SingleStoredWrapper<AmountStored> {
	
	@NonNull
	@NotNull
	private AmountStored stored;
	
	public SingleAmountStoredWrapper(@NonNull AmountStored stored) {
		this.stored = stored;
	}
	
	@Override
	public Quantity<?> recalcTotal() {
		return this.getTotal();
	}
	
	@Override
	public Quantity<?> getTotal() {
		return this.getStored().getAmount();
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
