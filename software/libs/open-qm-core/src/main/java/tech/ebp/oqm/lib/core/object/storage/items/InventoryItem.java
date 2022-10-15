package tech.ebp.oqm.lib.core.object.storage.items;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.ImagedMainObject;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NoStorageBlockException;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
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
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;
import tech.ebp.oqm.lib.core.object.storage.items.storedWrapper.StoredWrapper;
import tech.ebp.oqm.lib.core.object.storage.items.utils.QuantitySumHelper;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes a type of inventory item.
 * <p>
 * TODO:: simplify getStorageType for superBuilder, similar to how Service handles
 *
 * @param <S> The type of Stored object this deals with
 * @param <C> The general collection type wrapped by T
 * @param <W> The StoredWrapper used.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "storageType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = SimpleAmountItem.class, name = "AMOUNT_SIMPLE"),
	@JsonSubTypes.Type(value = ListAmountItem.class, name = "AMOUNT_LIST"),
	@JsonSubTypes.Type(value = TrackedItem.class, name = "TRACKED")
})
@BsonDiscriminator(key = "storedType_mongo")
public abstract class InventoryItem<S extends Stored, C, W extends StoredWrapper<C, S>> extends ImagedMainObject {
	
	/**
	 * The name of this inventory item
	 */
	@NonNull
	@NotBlank(message = "Name cannot be blank")
	private String name;
	
	/**
	 * The type of storage this item uses.
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public abstract StorageType getStorageType();
	
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
	private Map<@NonNull ObjectId, @NonNull W> storageMap = new LinkedHashMap<>();
	
	/**
	 * The total amount of that item in storage, in the {@link #getUnit()} unit.
	 */
	@Setter(AccessLevel.PROTECTED)
	private Quantity<?> total = null;
	
	@Setter(AccessLevel.PROTECTED)
	private BigDecimal valueOfStored = BigDecimal.ZERO;
	
	@NonNull
	@NotNull
	private Duration expiryWarningThreshold = Duration.ZERO;
	
	public abstract @NonNull Unit<?> getUnit();
	
	/**
	 * Recalculates the total amount of the item stored.
	 * <p>
	 * Calculates the total, sets {@link #total}, and returns the value.
	 *
	 * @return The total amount stored.
	 */
	public Quantity<?> recalcTotal() {
		QuantitySumHelper helper = new QuantitySumHelper(this.getUnit());
		
		helper.addAll(
			this.getStorageMap()
				.values()
				.stream()
				.map((W wrapper)->{
					wrapper.recalcDerived();
					return wrapper.getTotal();
				})
		);
		
		this.setTotal(helper.getTotal());
		return this.total;
	}
	
	public Quantity<?> getTotal() {
		if (total == null) {
			this.recalculateDerived();
		}
		return this.total;
	}
	
	/**
	 * Gets the number of individual entities stored
	 *
	 * @return the number of individual entities stored
	 */
	public long numStored() {
		return this.getStorageMap().values().stream().map(StoredWrapper::getNumStored).reduce(0L, Long::sum);
	}
	
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
			this.recalculateDerived();
		}
		return this.valueOfStored;
	}
	
	
	private long numExpired = 0;
	private long numExpiryWarn = 0;
	
	public abstract InventoryItem<S, C, W> recalculateExpiryStats();
	
	
	public InventoryItem<S, C, W> recalculateDerived() {
		this.recalcTotal();
		this.recalcValueOfStored();
		this.recalculateExpiryStats();
		return this;
	}
	
	/**
	 * Gets a new instance of what holds the T value.
	 *
	 * @return a new valid T instance
	 */
	protected abstract W newTInstance();
	
	protected W getStoredWrapperForStorage(ObjectId storageId, boolean addStorageBlockIdIfNone) {
		if (!this.getStorageMap().containsKey(storageId)) {
			if (addStorageBlockIdIfNone) {
				this.getStorageMap().put(storageId, this.newTInstance());
			} else {
				return null;
			}
		}
		return this.getStorageMap().get(storageId);
	}
	
	public W getStoredWrapperForStorage(ObjectId storageId) {
		return this.getStoredWrapperForStorage(storageId, true);
	}
	
	protected C getStoredForStorage(ObjectId storageId, boolean addStorageBlockIdIfNone) {
		W wrapper = this.getStoredWrapperForStorage(storageId, addStorageBlockIdIfNone);
		
		if (wrapper == null) {
			return null;
		}
		return wrapper.getStored();
	}
	
	public C getStoredForStorage(ObjectId storageId) {
		return this.getStoredForStorage(storageId, true);
	}
	
	
	/**
	 * Adds a stored to the set held at the storage block id given.
	 * <p>
	 * Semantics differ based on the general type of how things are stored for this item (C), but described below:
	 * <ul>
	 *     <li>
	 *         <strong>Single Amount</strong>- The amount of the parameter is added to the amount held.
	 *     </li>
	 *     <li>
	 *         <strong>Any collection</strong>- The Stored object given is added to the set alongside the others already stored
	 *     </li>
	 * </ul>
	 *
	 * @param storageId
	 * @param toAdd
	 * @param storageBlockStrict False if want to add storage block if not present. True if fail if storage block not present.
	 *
	 * @return
	 */
	public InventoryItem<S, C, W> add(ObjectId storageId, S toAdd, boolean addStorageBlockIdIfNone) throws NoStorageBlockException {
		W wrapper = this.getStoredWrapperForStorage(storageId, addStorageBlockIdIfNone);
		
		if (wrapper == null) {
			throw new NoStorageBlockException();
		}
		
		wrapper.addStored(toAdd);
		this.recalcTotal();
		return this;
	}
	
	/**
	 * Wrapper for {@link #add(ObjectId, S, boolean)}, with true passed to storageBlockStrict
	 *
	 * @param storageId
	 * @param toAdd
	 *
	 * @return
	 */
	public InventoryItem<S, C, W> add(ObjectId storageId, S toAdd) {
		return this.add(storageId, toAdd, true);
	}
	
	/**
	 * Adds a stored to the set held at the storage block id given.
	 * <p>
	 * Semantics differ based on the general type of how things are stored for this item (C), but described below:
	 * <ul>
	 *     <li>
	 *         <strong>Single Amount</strong>- The amount of the parameter is subtracted from the amount held.
	 *     </li>
	 *     <li>
	 *         <strong>Any collection</strong>- The Stored object given is removed from the set alongside the others already stored
	 *     </li>
	 * </ul>
	 *
	 * @param storageId
	 * @param toAdd
	 * @param storageBlockStrict False if want to add storage block if not present. True if fail if storage block not present.
	 *
	 * @return
	 * @throws NotEnoughStoredException If there isn't enough held to subtract
	 */
	public InventoryItem<S, C, W> subtract(ObjectId storageId, S toSubtract) throws NotEnoughStoredException, NoStorageBlockException {
		W wrapper = this.getStoredWrapperForStorage(storageId, false);
		
		if (wrapper == null) {
			throw new NoStorageBlockException();
		}
		
		wrapper.subtractStored(toSubtract);
		this.recalcTotal();
		return this;
	}
	
	public InventoryItem<S, C, W> transfer(ObjectId storageIdFrom, ObjectId storageIdTo, S t) throws NotEnoughStoredException {
		this.subtract(storageIdFrom, t);
		this.add(storageIdTo, t);
		
		return this;
	}
}
