package tech.ebp.oqm.lib.core.object.storage.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.ImagedMainObject;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryEvent;
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
import tech.ebp.oqm.lib.core.quantities.QuantitiesUtils;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Describes a type of inventory item.
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
	 * Maybe make this its own class?
	 */
	//    private Quantity<?> unitQuantity = null;
	
	/**
	 * Description of the item
	 */
	private String description = null;
	
	/**
	 * The map of where the items are stored.
	 * <p>
	 * The key is the id of the storage block being stored in, the value the storage wrapper actually holding stored item information.
	 */
	@NonNull
	@NotNull
	private Map<@NonNull ObjectId, @NonNull W> storageMap = new LinkedHashMap<>();
	
	/**
	 * The total amount of that item in storage, in the {@link #getUnit()} unit.
	 * <p>
	 * Calculated in {@link #recalculateDerived()}
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PROTECTED)
	private Quantity<?> total = null;
	
	//	/**
	//	 * Don't call this method
	//	 * @param total
	//	 * @return
	//	 */
	//	@Deprecated(forRemoval = false)
	//	public InventoryItem<S, C, W> setTotal(Quantity<?> total){
	//		this.total = total;
	//		return this;
	//	}
	
	/**
	 * The total value of everything stored.
	 * <p>
	 * Calculated in {@link #recalculateDerived()}
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PROTECTED)
	private BigDecimal valueOfStored = BigDecimal.ZERO;
	
	
	private ItemNotificationStatus notificationStatus = new ItemNotificationStatus();
	
	/**
	 * When before a stored item expired to send a warning out about that expiration.
	 * <p>
	 * {@link Duration#ZERO} for no expiration.
	 */
	@NonNull
	@NotNull
	private Duration expiryWarningThreshold = Duration.ZERO;
	
	/**
	 * The threshold of low stock for the entire object.
	 * <p>
	 * See {@link StoredWrapper#getLowStockThreshold()} for low stock warnings on individual blocks.
	 * <p>
	 * Null for no threshold, Quantity with compatible unit to set the threshold.
	 */
	private Quantity lowStockThreshold = null;
	
	/**
	 * The number of low stock storage blocks, also including overall.
	 * <p>
	 * Calculated in {@link #recalculateDerived()}
	 */
	@Setter(AccessLevel.PUBLIC)
	private long numLowStock = 0;
	
	public List<ItemLowStockEvent> updateLowStockState() {
		List<ItemLowStockEvent> output = new ArrayList<>();
		int newNumLowStock = 0;
		
		Quantity total = this.recalcTotal();
		Quantity lowStockThreshold = this.getLowStockThreshold();
		
		if (QuantitiesUtils.isLowStock(total, lowStockThreshold)) {
			boolean previouslyLow = this.getNotificationStatus().isLowStock();
			this.getNotificationStatus().setLowStock(true);
			newNumLowStock++;
			
			if (!previouslyLow) {
				output.add(
					ItemLowStockEvent.builder()
									 .build()
				);
			}
		} else {
			this.getNotificationStatus().setLowStock(false);
		}
		
		for (Map.Entry<ObjectId, W> curEntry : this.getStorageMap().entrySet()) {
			Optional<ItemLowStockEvent.Builder<?, ?>> result = curEntry.getValue().updateLowStockState();
			
			if (curEntry.getValue().getNotificationStatus().isLowStock()) {
				newNumLowStock++;
			}
			
			//noinspection OptionalIsPresent
			if (result.isPresent()) {
				output.add(
					result.get().storageBlockId(curEntry.getKey()).build()
				);
			}
		}
		
		this.setNumLowStock(newNumLowStock);
		return output;
	}
	
	/**
	 * The unit to associate with this item. Stored items can have different units, but must be compatible with this one.
	 *
	 * @return The unit associated with this item.
	 */
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
	
	/**
	 * The number of expired stored items held.
	 * <p>
	 * Calculated in {@link #recalculateExpiryDerivedStats()}
	 */
	@Setter(AccessLevel.PROTECTED)
	private long numExpired = 0;
	
	/**
	 * The number of stored items close to expiring held.
	 * <p>
	 * Calculated in {@link #recalculateExpiryDerivedStats()}
	 */
	@Setter(AccessLevel.PROTECTED)
	private long numExpiryWarn = 0;
	
	
	public List<ItemExpiryEvent> updateExpiredStates() {
		List<ItemExpiryEvent> output =
			this.getStorageMap().entrySet().stream()
				.map((Map.Entry<ObjectId, W> wrapperEntry)->{
					return wrapperEntry.getValue().updateExpiredStates(wrapperEntry.getKey(), getExpiryWarningThreshold());
				})
				.flatMap(List::stream)
				.collect(Collectors.toList());
		this.recalculateExpiryDerivedStats();
		return output;
	}
	
	public InventoryItem<S, C, W> recalculateExpiryDerivedStats() {
		AtomicLong expiredSum = new AtomicLong();
		AtomicLong expiryWarnSum = new AtomicLong();
		
		this.getStorageMap().values().forEach(
			(StoredWrapper w)->{
				expiredSum.addAndGet(w.getNumExpired());
				expiryWarnSum.addAndGet(w.getNumExpiryWarned());
			}
		);
		
		this.setNumExpired(expiredSum.get());
		this.setNumExpiryWarn(expiryWarnSum.get());
		return this;
	}
	
	/**
	 * Recalculates all derived stats for this object.
	 * <p>
	 * Should call this whenever you are done making changes to stored items.
	 *
	 * @return This object
	 */
	public InventoryItem<S, C, W> recalculateDerived() {
		this.getStorageMap().values().parallelStream().forEach(StoredWrapper::recalcDerived);
		
		this.recalcTotal();
		this.recalcValueOfStored();
		this.recalculateExpiryDerivedStats();
		return this;
	}
	
	/**
	 * Gets a new valid wrapper instance.
	 *
	 * @return a new valid wrapper instance
	 */
	protected abstract W newWrapperInstance();
	
	/**
	 * Gets the storage wrapper for the given storage block id.
	 *
	 * @param storageId The id of the storage block we are dealing with.
	 * @param addStorageBlockIdIfNone True if when the storage is not associated, add it.
	 *
	 * @return The wrapper at the storage block held, <pre>null</pre> if not held (unless addStorageBlockIdIfNone is true)
	 */
	public W getStoredWrapperForStorage(ObjectId storageId, boolean addStorageBlockIdIfNone) {
		if (!this.getStorageMap().containsKey(storageId)) {
			if (addStorageBlockIdIfNone) {
				this.getStorageMap().put(storageId, this.newWrapperInstance());
			} else {
				return null;
			}
		}
		return this.getStorageMap().get(storageId);
	}
	
	/**
	 * Gets the storage wrapper for the given storage block id.
	 * <p>
	 * Wrapper for {@link #getStoredWrapperForStorage(ObjectId, boolean)}, with addStorageBlockIdIfNone set to <pre>true</pre>.
	 *
	 * @param storageId The id of the storage block we are dealing with.
	 *
	 * @return The wrapper at the storage block held
	 */
	public W getStoredWrapperForStorage(ObjectId storageId) {
		return this.getStoredWrapperForStorage(storageId, true);
	}
	
	/**
	 * Gets the storage collection for the given storage block id.
	 *
	 * @param storageId The id of the storage block we are dealing with.
	 * @param addStorageBlockIdIfNone True if when the storage is not associated, add it.
	 *
	 * @return The collection at the storage block held, <pre>null</pre> if not held (unless addStorageBlockIdIfNone is true)
	 */
	protected C getStoredForStorage(ObjectId storageId, boolean addStorageBlockIdIfNone) {
		W wrapper = this.getStoredWrapperForStorage(storageId, addStorageBlockIdIfNone);
		
		if (wrapper == null) {
			return null;
		}
		return wrapper.getStored();
	}
	
	/**
	 * Gets the storage collection for the given storage block id.
	 * <p>
	 * Wrapper for {@link #getStoredForStorage(ObjectId, boolean)}, with addStorageBlockIdIfNone set to <pre>true</pre>.
	 *
	 * @param storageId The id of the storage block we are dealing with.
	 *
	 * @return The collection at the storage block held
	 */
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
	 * @param storageId The id of the storage block we are dealing with.
	 * @param toAdd The stored object to add/ add with
	 * @param addStorageBlockIdIfNone True if when the storage is not associated, add it.
	 *
	 * @return This object
	 * @throws NoStorageBlockException If no storage held at storage block, and addStorageBlockIdIfNone is false
	 */
	public InventoryItem<S, C, W> add(ObjectId storageId, S toAdd, boolean addStorageBlockIdIfNone) throws NoStorageBlockException {
		W wrapper = this.getStoredWrapperForStorage(storageId, addStorageBlockIdIfNone);
		
		if (wrapper == null) {
			throw new NoStorageBlockException();
		}
		
		wrapper.addStored(toAdd);
		return this;
	}
	
	/**
	 * Wrapper for {@link #add(ObjectId, Stored, boolean)}, with true passed to storageBlockStrict
	 *
	 * @param storageId The id of the storage block we are dealing with.
	 * @param toAdd The stored object to add/ add with
	 *
	 * @return This object
	 */
	public InventoryItem<S, C, W> add(ObjectId storageId, S toAdd) {
		return this.add(storageId, toAdd, true);
	}
	
	/**
	 * Subtracts a stored from the set held at the storage block id given.
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
	 * @param storageId The id of the storage block we are dealing with.
	 * @param toSubtract The stored object to remove/ use to subtract with
	 *
	 * @return This object
	 * @throws NotEnoughStoredException If there isn't enough held to subtract, or if the stored object does not exist
	 */
	public InventoryItem<S, C, W> subtract(ObjectId storageId, S toSubtract) throws NotEnoughStoredException, NoStorageBlockException {
		W wrapper = this.getStoredWrapperForStorage(storageId, false);
		
		if (wrapper == null) {
			throw new NoStorageBlockException();
		}
		
		wrapper.subtractStored(toSubtract);
		return this;
	}
	
	/**
	 * Transfers a stored item from one storage block to another.
	 * <p>
	 * Just calls {@link #subtract(ObjectId, Stored)}, then {@link #add(ObjectId, Stored)}
	 *
	 * @param storageIdFrom The id of the storage block we are taking from
	 * @param storageIdTo The id of the storage block we are adding to
	 * @param toTransfer The stored amount to transfer.
	 *
	 * @return This object
	 * @throws NotEnoughStoredException
	 */
	public InventoryItem<S, C, W> transfer(ObjectId storageIdFrom, ObjectId storageIdTo, S toTransfer) throws NotEnoughStoredException {
		this.subtract(storageIdFrom, toTransfer);
		this.add(storageIdTo, toTransfer);
		
		return this;
	}
	
	/**
	 * Gets a stream of all stored items held
	 *
	 * @return a stream of all stored items held
	 */
	public Stream<S> storedStream() {
		return this.storageMap.values().stream().flatMap(StoredWrapper::storedStream);
	}
}
