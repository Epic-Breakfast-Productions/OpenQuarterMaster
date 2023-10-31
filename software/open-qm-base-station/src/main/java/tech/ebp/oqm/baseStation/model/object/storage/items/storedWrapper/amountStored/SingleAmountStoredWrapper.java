package tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.amountStored;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.baseStation.model.object.storage.items.exception.StoredNotFoundException;
import tech.ebp.oqm.baseStation.model.object.storage.items.exception.UnsupportedStoredOperationException;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper.SingleStoredWrapper;

import javax.measure.Quantity;
import java.util.UUID;

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
	public void addStored(UUID storedId, AmountStored stored) throws UnsupportedStoredOperationException {
		if(!this.getStored().getId().equals(storedId)){
			throw new UnsupportedStoredOperationException("Cannot add amount to a different stored in a plain amount. (Stored id given was != the id of the stored held at this "
														  + "storage block)");
		}
		this.addStored(stored);
	}
	
	@Override
	public AmountStored subtractStored(AmountStored stored) throws NotEnoughStoredException {
		return this.getStored().subtract(stored);
	}
	
	@Override
	public AmountStored subtractStored(UUID storedId, AmountStored stored) throws NotEnoughStoredException, StoredNotFoundException {
		if(!this.getStored().getId().equals(storedId)){
			throw new UnsupportedStoredOperationException("Cannot subtract amount to a different stored in a plain amount. (Stored id given was != the id of the stored held at "
														  + "this storage block)");
		}
		return this.subtractStored(stored);
	}
	
	/**
	 * Removes all of the amount held.
	 *
	 * @param stored Ignored.
	 * @return
	 * @throws NotEnoughStoredException
	 */
//	@Override
	public AmountStored subtractStored(UUID stored) throws NotEnoughStoredException {
		AmountStored output = new AmountStored();
		
		output.setAmount(this.getStored().getAmount());
		
		this.getStored().setAmount(0, this.getStored().getAmount().getUnit());
		
		return this.getStored();
	}
}
