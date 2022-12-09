package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemLowStockEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.ItemStorageBlockEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.measure.Quantity;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Wrapper that helps hold extra information dealing with the json form of inventory item objects.
 *
 * @param <T> The storage type wrapped.
 * @param <S> The type held in the storage
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class StoredWrapper<T, S extends Stored> {
	
	@NonNull
	@NotNull
	private WrapperNotificationStatus notificationStatus = new WrapperNotificationStatus();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PROTECTED)
	private Quantity total = null;
	
	@Min(0L)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PROTECTED)
	private long numExpired = 0L;
	
	@Min(0L)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Setter(AccessLevel.PROTECTED)
	private long numExpiryWarned = 0L;
	
	private Quantity lowStockThreshold = null;
	
	//TODO:: review, make similar to expiry, test
	public Optional<ItemLowStockEvent.Builder<?, ?>> updateLowStockState(ObjectId blockKey) {
		this.recalcTotal();
		if (this.getLowStockThreshold() == null || this.total == null) {
			return Optional.empty();
		}
		
		//		if (
		//			this.getTotal()
		//		) {
		//			boolean previouslyLow = this.getNotificationStatus().isLowStock();
		//			this.getNotificationStatus().setLowStock(true);
		//
		//			if (!previouslyLow) {
		//				return Optional.of(
		//					ItemLowStockEvent.builder().storageBlockId(blockKey)
		//				);
		//			}
		//			return Optional.empty();
		//		} else {
		//			this.getNotificationStatus().setLowStock(false);
		//		}
		
		return Optional.empty();
	}
	
	//TODO:: implement recalc similar to total. Deal with getting val per unit from parent.
	//	@Setter(AccessLevel.PROTECTED)
	//	private BigDecimal totalValue = null;
	
	public abstract T getStored();
	
	/**
	 * Gets the number of individual items stored in the wrapper
	 *
	 * @return
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public abstract long getNumStored();
	
	public abstract Stream<S> storedStream();
	
	/**
	 * Method to recalculate and return the total amount held in this wrapper.
	 *
	 * @return The newly calculated total in this wrapper.
	 */
	protected abstract Quantity<?> recalcTotal();
	
	public Quantity<?> getTotal() {
		if (total == null) {
			this.recalcTotal();
		}
		return this.total;
	}
	
	protected static Optional<ItemExpiryEvent.Builder<?, ?>> updateExpiredStateForStored(
		Stored stored,
		ObjectId blockKey,
		Duration expiredWarningThreshold
	) {
		if (stored.getExpires() == null) {
			return Optional.empty();
		}
		
		
		if (
			LocalDateTime.now().isAfter(stored.getExpires())
		) {
			boolean previouslyExpired = stored.getNotificationStatus().isExpired();
			stored.getNotificationStatus().setExpired(true);
			
			if (!previouslyExpired) {
				stored.getNotificationStatus().setExpiredWarning(false);
				return Optional.of(
					ItemExpiredEvent.builder().storageBlockId(blockKey)
				);
			}
			return Optional.empty();
		} else {
			stored.getNotificationStatus().setExpired(false);
		}
		
		
		if (
			!Duration.ZERO.equals(expiredWarningThreshold) &&
			LocalDateTime.now().isAfter(stored.getExpires().minus(expiredWarningThreshold))
		) {
			boolean previouslyExpiredWarn = stored.getNotificationStatus().isExpiredWarning();
			stored.getNotificationStatus().setExpired(true);
			
			if (!previouslyExpiredWarn) {
				stored.getNotificationStatus().setExpiredWarning(true);
				return Optional.of(
					ItemExpiryWarningEvent.builder().storageBlockId(blockKey)
				);
			}
		} else {
			stored.getNotificationStatus().setExpiredWarning(false);
		}
		
		return Optional.empty();
	}
	
	
	public abstract List<ItemExpiryEvent> updateExpiredStates(ObjectId blockKey, Duration expiredWarningThreshold);
	
	/**
	 * Recalculates expired related stats, not if things are actually expired
	 */
	public void recalculateExpiredRelated() {
		AtomicLong newExpiredCount = new AtomicLong();
		AtomicLong newExpiryWarnCount = new AtomicLong();
		
		this.storedStream()
			.forEach((Stored s)->{
				if (s.getNotificationStatus().isExpired()) {
					newExpiredCount.getAndIncrement();
				} else if (s.getNotificationStatus().isExpiredWarning()) {
					newExpiryWarnCount.getAndIncrement();
				}
			});
		
		this.setNumExpired(newExpiredCount.get());
		this.setNumExpiryWarned(newExpiryWarnCount.get());
	}
	
	public StoredWrapper<T, S> recalcDerived() {
		this.recalcTotal();
		this.recalculateExpiredRelated();
		return this;
	}
	
	/**
	 * Adds to the stored. Semantics based on implementation:
	 * <ul>
	 *     <li>
	 *         <strong>Single Amount</strong>- The amount of the parameter is added to the amount held.
	 *     </li>
	 *     <li>
	 *         <strong>Any collection</strong>- The Stored object given is added to the set alongside the others already stored
	 *     </li>
	 * </ul>
	 *
	 * @param stored
	 */
	public abstract void addStored(S stored);
	
	/**
	 * Removes from the stored. Semantics based on implementation:
	 * <ul>
	 *     <li>
	 *         <strong>Single Amount</strong>- The amount of the parameter is subtracted from the amount held.
	 *     </li>
	 *     <li>
	 *         <strong>Any collection</strong>- The Stored object given is removed from the set alongside the others already stored
	 *     </li>
	 * </ul>
	 *
	 * @param stored
	 *
	 * @return
	 * @throws NotEnoughStoredException When there is not enough stored to remove
	 */
	public abstract S subtractStored(S stored) throws NotEnoughStoredException;
}
