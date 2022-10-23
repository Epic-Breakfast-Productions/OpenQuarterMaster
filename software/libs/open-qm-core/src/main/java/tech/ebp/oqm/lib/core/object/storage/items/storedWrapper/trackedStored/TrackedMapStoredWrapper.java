package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.trackedStored;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.units.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.exception.AlreadyStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.MapStoredWrapper;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

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
		this.setTotal(Quantities.getQuantity(this.values().size(), UnitUtils.UNIT));
		return this.getTotal();
	}
	
	@Override
	public void addStored(TrackedStored stored) throws AlreadyStoredException {
		super.addStored(stored.getIdentifier(), stored);
	}
	
	@Override
	public TrackedStored subtractStored(TrackedStored stored) throws NotEnoughStoredException {
		return this.subtractStored(stored.getIdentifier());
	}
}
