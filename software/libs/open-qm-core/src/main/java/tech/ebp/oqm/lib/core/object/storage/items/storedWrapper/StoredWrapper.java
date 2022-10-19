package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiredEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryWarningEvent;
import tech.ebp.oqm.lib.core.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.measure.Quantity;
import javax.validation.constraints.Min;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO:: abstract method to stream all stored
 *
 * @param <T> The storage type wrapped.
 * @param <S> The type held in the storage
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class StoredWrapper<T, S extends Stored> {
	
	@Setter(AccessLevel.PROTECTED)
	private Quantity<?> total = null;
	
	@Min(0L)
	@Setter(AccessLevel.PROTECTED)
	private long numExpired = 0L;
	@Min(0L)
	@Setter(AccessLevel.PROTECTED)
	private long numExpiryWarned = 0L;
	
	
	//TODO:: implement recalc similar to total. Deal with getting val per unit from parent.
	//	@Setter(AccessLevel.PROTECTED)
	//	private BigDecimal totalValue = null;
	
	//	@NonNull
	//	@NotNull
	//	private T stored;
	//
	//	protected StoredWrapper(T stored) {
	//		this.stored = stored;
	//	}
	
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
	
	protected Optional<ItemExpiryEvent.Builder<?, ?>> updateExpiredStateForStored(
		Stored stored,
		ObjectId blockKey,
		Duration expiredWarningThreshold
	) {
		if (stored.getExpires() == null) {
			return Optional.empty();
		}
		
		stored.getNotificationStatus().setExpired(false);
		stored.getNotificationStatus().setExpiredWarning(false);
		
		if (
			!stored.getNotificationStatus().isExpired() &&
			LocalDateTime.now().isAfter(stored.getExpires())
		) {
			stored.getNotificationStatus().setExpired(true);
			return Optional.of(
				ItemExpiredEvent.builder().storageBlockId(blockKey)
			);
		} else if (
				   !stored.getNotificationStatus().isExpiredWarning() &&
				   !stored.getNotificationStatus().isExpired() &&
				   !Duration.ZERO.equals(expiredWarningThreshold) &&
				   LocalDateTime.now().isAfter(stored.getExpires().minus(expiredWarningThreshold))
		) {
			stored.getNotificationStatus().setExpiredWarning(true);
			return Optional.of(
				ItemExpiryWarningEvent.builder().storageBlockId(blockKey)
			);
		}
		
		return Optional.empty();
	}
	
	
	public abstract List<ItemExpiryEvent> updateExpiredStates(ObjectId blockKey, Duration expiredWarningThreshold);
	
	public abstract void recalculateExpiredRelated();
	
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
