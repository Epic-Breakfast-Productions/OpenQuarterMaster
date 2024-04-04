package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.trackedStored;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.AlreadyStoredException;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.StoredNotFoundException;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.UnsupportedStoredOperationException;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper.MapStoredWrapper;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TODO:: validator to ensure identifiers == stored identifiers
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrackedMapStoredWrapper extends MapStoredWrapper<TrackedStored> {
	
	@NonNull
	@NotNull
	private Map<@NotBlank String, @NotNull TrackedStored> stored = new HashMap<>();
	
	@Override
	public Quantity<?> recalcTotal() {
		this.setTotal(Quantities.getQuantity(this.values().size(), OqmProvidedUnits.UNIT));
		return this.getTotal();
	}
	
	@Override
	public void addStored(TrackedStored stored) throws AlreadyStoredException {
		super.addStored(stored.getIdentifier(), stored);
	}
	
	@Override
	public void addStored(UUID storedId, TrackedStored stored) {
		throw new UnsupportedStoredOperationException("Cannot add to a unique tracked item.");
	}
	
	@Override
	public TrackedStored subtractStored(TrackedStored stored) throws NotEnoughStoredException {
		return this.subtractStored(stored.getIdentifier());
	}
	
	@Override
	public TrackedStored subtractStored(UUID storedId, TrackedStored stored) throws NotEnoughStoredException, StoredNotFoundException {
		throw new UnsupportedStoredOperationException("Cannot subtract from a unique tracked item.");
	}
}
