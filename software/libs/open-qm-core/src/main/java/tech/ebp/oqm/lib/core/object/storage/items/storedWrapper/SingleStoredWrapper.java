package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.measure.Quantity;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class SingleStoredWrapper<T extends Stored> extends StoredWrapper<T, T> {
	
	//	protected SingleStoredWrapper(T stored) {
	//		super(stored);
	//	}
	
	
	@Override
	public Stream<T> storedStream() {
		return Stream.of(this.getStored());
	}
	
	@Override
	public long getNumStored() {
		return 1;
	}
	
	@Override
	public List<ItemExpiryEvent> updateExpiredStates(ObjectId blockKey, Duration expiredWarningThreshold) {
		Optional<ItemExpiryEvent.Builder<?, ?>> output = this.updateExpiredStateForStored(
			this.getStored(),
			blockKey,
			expiredWarningThreshold
		);
		
		if (output.isEmpty()) {
			return List.of();
		}
		
		return List.of(output.get().build());
	}
	
	@Override
	public void recalculateExpiredRelated() {
		long expiredResult = 0;
		long expiryWarnResult = 0;
		
		if (this.getStored().getNotificationStatus().isExpired()) {
			expiredResult = 1;
		} else if (this.getStored().getNotificationStatus().isExpiredWarning()) {
			expiryWarnResult = 1;
		}
		
		this.setNumExpired(expiredResult);
		this.setNumExpiryWarned(expiryWarnResult);
	}
}
