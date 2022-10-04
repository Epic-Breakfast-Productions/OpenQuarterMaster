package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.measure.Quantity;
import javax.validation.constraints.NotNull;
import java.util.stream.Stream;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class StoredWrapper<T, S extends Stored> {
	
	@Setter(AccessLevel.PROTECTED)
	private Quantity<?> total = null;
	
	@Setter(AccessLevel.PROTECTED)
	private Quantity<?> totalCost = null;
	
	@NonNull
	@NotNull
	private T stored;
	
	protected StoredWrapper(T stored) {
		this.stored = stored;
	}
	
	/**
	 * Gets the number of individual items stored in the wrapper
	 *
	 * @return
	 */
	public abstract long getNumStored();
	
	/**
	 * Method to recalculate and return the total amount held in this wrapper.
	 *
	 * @return The newly calculated total in this wrapper.
	 */
	public abstract Quantity<?> recalcTotal();
	
	public Quantity<?> getTotal() {
		if (total == null) {
			this.recalcTotal();
		}
		return this.total;
	}
	
	/**
	 * Adds to the stored. Semantics based on implementation.
	 *
	 * @param stored
	 */
	public abstract void addStored(S stored);
	
	/**
	 * Removes from the stored. Semantics based on implementation.
	 *
	 * @param stored
	 *
	 * @return
	 * @throws NotEnoughStoredException When there is not enough stored to remove
	 */
	public abstract S subtractStored(S stored) throws NotEnoughStoredException;
}
