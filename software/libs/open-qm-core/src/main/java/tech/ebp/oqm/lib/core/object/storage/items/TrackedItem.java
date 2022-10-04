package tech.ebp.oqm.lib.core.object.storage.items;

import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.trackedStored.TrackedMapStoredWrapper;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes a unique item stored.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrackedItem extends InventoryItem<TrackedStored, Map<String, TrackedStored>, TrackedMapStoredWrapper> {
	
	@Override
	public StorageType getStorageType() {
		return StorageType.TRACKED;
	}
	
	/**
	 * The name of the identifier used for the items tracked.
	 * <p>
	 * Example might be serial number.
	 */
	@NonNull
	@NotNull
	@Size(max = 50)
	@NotBlank
	private String trackedItemIdentifierName;
	
	/**
	 * The default value of an item held, if unspecified.
	 */
	@NonNull
	@DecimalMin("0.0")
	private BigDecimal defaultValue = BigDecimal.ZERO;
	
	
	@BsonIgnore
	@JsonIgnore
	@Override
	public @NonNull Unit<?> getUnit() {
		return UnitUtils.UNIT;
	}
	
	@Override
	protected TrackedMapStoredWrapper newTInstance() {
		return new TrackedMapStoredWrapper();
	}
}
