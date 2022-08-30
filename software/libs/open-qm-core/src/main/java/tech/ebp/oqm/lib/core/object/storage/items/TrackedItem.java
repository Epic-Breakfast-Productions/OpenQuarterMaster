package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.UnitUtils;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import tech.ebp.oqm.lib.core.object.storage.items.stored.TrackedStored;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
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
@EqualsAndHashCode(callSuper = true)
@Data
public class TrackedItem extends InventoryItem<Map<@NotBlank String, @NotNull TrackedStored>> {
	
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
	
	public TrackedItem() {
		super(StorageType.TRACKED);
	}
	
	@BsonIgnore
	@JsonIgnore
	@Override
	public @NonNull Unit<?> getUnit() {
		return UnitUtils.UNIT;
	}
	
	@Override
	public Quantity<?> recalcTotal() {
		this.setTotal(
			Quantities.getQuantity(
				this.getStorageMap()
					.values()
					.parallelStream()
					.mapToLong(Map::size)
					.sum(),
				this.getUnit()
			)
		);
		return this.getTotal();
	}
	
	@Override
	public long numStored() {
		return (long) this.getTotal().getValue();
	}
	
	@Override
	public BigDecimal recalcValueOfStored() {
		this.recalcTotal();
		this.setValueOfStored(
			this.getStorageMap()
				.values()
				.stream()
				.flatMap((map)->map.values().stream())
				.map(TrackedStored::getValue)
				.map((BigDecimal d)->d == null ? this.getDefaultValue() : d) //TODO:: test this line
				.reduce(BigDecimal.ZERO, BigDecimal::add)
		);
		return this.getValueOfStored();
	}
	
	@Override
	protected Map<@NotBlank String, @NotNull TrackedStored> newTInstance() {
		return new HashMap<>();
	}
	
	public TrackedItem add(ObjectId storageId, @NonNull String identifier, @NonNull TrackedStored stored, boolean storageBlockStrict) {
		Map<String, TrackedStored> map = this.getStoredForStorage(storageId, !storageBlockStrict);
		
		if (map == null) {
			//TODO:: custom exception
			throw new IllegalArgumentException("No storage block found with that Id.");
		}
		
		if (map.containsKey(identifier)) {
			throw new IllegalArgumentException("Item with that identifier already exists.");
		}
		map.put(identifier, stored);
		this.recalcTotal();
		return this;
	}
	
	public TrackedItem add(ObjectId storageId, @NonNull String identifier, @NonNull TrackedStored stored) {
		return this.add(storageId, identifier, stored, false);
	}
	
	@Override
	public InventoryItem<Map<String, TrackedStored>> add(
		ObjectId storageId,
		Map<String, TrackedStored> toAdd,
		boolean storageBlockStrict
	) {
		Map<String, TrackedStored> map = this.getStoredForStorage(storageId, !storageBlockStrict);
		
		if (map == null) {
			//TODO:: custom exception
			throw new IllegalArgumentException("No storage block found with that Id.");
		}
		
		//validate new additions
		for (Map.Entry<String, TrackedStored> curToAdd : toAdd.entrySet()) {
			if (map.containsKey(curToAdd.getKey())) {
				//TODO:: custom exception
				throw new IllegalArgumentException("Already contains a value for " + curToAdd.getKey());
			}
		}
		
		map.putAll(toAdd);
		
		this.recalcTotal();
		return this;
	}
	
	@Override
	public InventoryItem<Map<String, TrackedStored>> subtract(
		ObjectId storageId,
		Map<String, TrackedStored> toSubtract
	) {
		Map<String, TrackedStored> map = this.getStoredForStorage(storageId, false);
		
		if (map == null) {
			//TODO:: custom exception
			throw new IllegalArgumentException("No storage block found with that Id.");
		}
		
		//validate to remove
		for (Map.Entry<String, TrackedStored> curToRem : toSubtract.entrySet()) {
			if (map.containsKey(curToRem.getKey())) {
				//TODO:: custom exception
				throw new IllegalArgumentException("Does not contain a value for " + curToRem.getKey() + " to remove.");
			}
		}
		
		for (Map.Entry<String, TrackedStored> curToRem : toSubtract.entrySet()) {
			map.remove(curToRem.getKey());
		}
		
		this.recalcTotal();
		return this;
	}
}
