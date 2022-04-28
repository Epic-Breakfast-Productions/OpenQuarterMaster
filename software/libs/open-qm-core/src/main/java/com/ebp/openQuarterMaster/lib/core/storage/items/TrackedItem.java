package com.ebp.openQuarterMaster.lib.core.storage.items;

import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.storage.items.stored.TrackedStored;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.units.indriya.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;
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
@BsonDiscriminator("TRACKED")
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
	
	public TrackedItem() {
		super(StoredType.TRACKED);
	}
	
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
	public BigDecimal valueOfStored() {
		return this.getStorageMap()
				   .values()
				   .stream()
				   .flatMap((map)->{
					   return map.values().stream();
				   })
				   .map(TrackedStored::getValue)
				   .reduce(BigDecimal.ZERO, BigDecimal::add);
	}
	
	@Override
	protected Map<@NotBlank String, @NotNull TrackedStored> newTInstance() {
		return new HashMap<>();
	}
	
	public TrackedItem add(ObjectId storageId, String identifier, TrackedStored stored) {
		Map<String, TrackedStored> map = this.getStoredForStorage(storageId);
		
		if (map.containsKey(identifier)) {
			throw new IllegalArgumentException("Item with that identifier already exists.");
		}
		map.put(identifier, stored);
		this.recalcTotal();
		return this;
	}
}
