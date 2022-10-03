package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.trackedStored;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.exception.AlreadyStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.MapStoredWrapper;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrackedMapStoredWrapper extends MapStoredWrapper<TrackedStored> {
	
	@Override
	public Quantity<?> recalcTotal() {
		this.setTotal(Quantities.getQuantity(this.values().size(), UnitUtils.UNIT));
		return this.getTotal();
	}
	
	@Override
	public void addStored(TrackedStored stored) throws AlreadyStoredException {
		if (this.containsKey(stored.getIdentifier())) {
			throw new AlreadyStoredException("Identifier already present: " + stored.getIdentifier());
		}
		this.put(stored.getIdentifier(), stored);
	}
	
	@Override
	public TrackedStored subtractStored(TrackedStored stored) throws NotEnoughStoredException {
		if (!this.containsKey(stored.getIdentifier())) {
			throw new NotEnoughStoredException("Identifier not stored: " + stored.getIdentifier());
		}
		return this.remove(stored.getIdentifier());
	}
}
