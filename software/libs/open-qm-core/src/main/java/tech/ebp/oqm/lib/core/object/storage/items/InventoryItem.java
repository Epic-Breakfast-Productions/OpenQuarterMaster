package tech.ebp.oqm.lib.core.object.storage.items;

import tech.ebp.oqm.lib.core.object.ImagedMainObject;
import tech.ebp.oqm.lib.core.object.storage.items.stored.StorageType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes a type of inventory item.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "storageType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = SimpleAmountItem.class, name = "AMOUNT_SIMPLE"),
	@JsonSubTypes.Type(value = ListAmountItem.class, name = "AMOUNT_LIST"),
	@JsonSubTypes.Type(value = TrackedItem.class, name = "TRACKED")
})
@BsonDiscriminator(key="storedType_mongo")
public abstract class InventoryItem<T> extends ImagedMainObject {
	
	/**
	 * The name of this inventory item
	 */
	@NonNull
	@NotBlank(message = "Name cannot be blank")
	private String name;
	
	/**
	 * The type of storage this item uses.
	 */
	@NonNull
	@NotNull
	private final StorageType storageType;
	
	/*TODO:: this useful?
	 * Used to determine how much of something is stored if {@link #unit} is {@link AbstractUnit#ONE}
	 * <p>
	 * Example, if we store gallon jugs of Milk, we could use this to specify each jug holds a gallon, and compute we have n gallons of milk
	 */
	//    private Quantity<?> unitQuantity = null;
	
	/**
	 * Description of the item
	 */
	private String description = null;
	
	/**
	 * The map of where the items are stored.
	 */
	@NonNull
	@NotNull
	private Map<@NonNull ObjectId, @NonNull T> storageMap = new LinkedHashMap<>();
	
	/**
	 * The total amount of that item in storage, in the {@link #getUnit()} unit.
	 */
	@Setter(AccessLevel.PROTECTED)
	private Quantity<?> total = null;
	
	@Setter(AccessLevel.PROTECTED)
	private BigDecimal valueOfStored = null;
	
	//TODO:: expiryWarningThreshold: duration before expiry dates when to warn that is expiring soon
	
	/**
	 * Constructor to make Lombok happy.
	 * <p>
	 * Disregard, not intended to be used
	 */
	public InventoryItem() {
		//noinspection ConstantConditions
		this.storageType = null;
		throw new UnsupportedOperationException();
	}
	
	protected InventoryItem(StorageType type) {
		this.storageType = type;
	}
	
	public abstract @NonNull Unit<?> getUnit();
	
	/**
	 * Recalculates the total amount of the item stored.
	 * <p>
	 * Calculates the total, sets {@link #total}, and returns the value.
	 *
	 * @return The total amount stored.
	 */
	public abstract Quantity<?> recalcTotal();
	
	public Quantity<?> getTotal() {
		if (total == null) {
			this.recalcTotal();
		}
		return this.total;
	}
	
	/**
	 * Gets the number of individual entities stored
	 *
	 * @return the number of individual entities stored
	 */
	public abstract long numStored();
	
	/**
	 * First calls {@link #recalcTotal()}, then calculates the total value of all items stored according to how it should for the type of
	 * inventory item, and finishes by setting {@link #valueOfStored}.
	 *
	 * @return The total that was calculated.
	 */
	public abstract BigDecimal recalcValueOfStored();
	
	/**
	 * Gets the total value of all items/ amounts stored.
	 *
	 * @return The value of all items/amounts stored.
	 */
	public BigDecimal getValueOfStored() {
		if (this.valueOfStored == null) {
			this.recalcValueOfStored();
		}
		return this.valueOfStored;
	}
	
	/**
	 * Gets a new instance of what holds the T value.
	 * @return
	 */
	protected abstract T newTInstance();
	
	protected T getStoredForStorage(ObjectId storageId, boolean createIfNone) {
		if (!this.getStorageMap().containsKey(storageId)) {
			if (createIfNone) {
				this.getStorageMap().put(storageId, this.newTInstance());
			} else {
				return null;
			}
		}
		return this.getStorageMap().get(storageId);
	}
	
	protected T getStoredForStorage(ObjectId storageId) {
		return this.getStoredForStorage(storageId, true);
	}
	
	/**
	 *
	 * @param storageId
	 * @param toAdd
	 * @param storageBlockStrict False if want to add storage block if not present. True if fail if storage block not present.
	 * @return
	 */
	public abstract InventoryItem<T> add(ObjectId storageId, T toAdd, boolean storageBlockStrict);
	
	/**
	 * Wrapper for {@link #add(ObjectId, Object, boolean)}, with false passed to storageBlockStrict
	 * @param storageId
	 * @param toAdd
	 * @return
	 */
	public InventoryItem<T> add(ObjectId storageId, T toAdd) {
		return this.add(storageId, toAdd, false);
	}
	
	public abstract InventoryItem<T> subtract(ObjectId storageId, T toSubtract);
	
	public InventoryItem<T> transfer(ObjectId storageIdFrom, ObjectId storageIdTo, T t) {
		this.subtract(storageIdFrom, t);
		this.add(storageIdTo, t);
		
		this.recalcTotal();
		return this;
	}
}
