package tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;

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
		Optional<ItemExpiryEvent> output = updateExpiredStateForStored(
			this.getStored(),
			blockKey,
			expiredWarningThreshold
		);
		
		if (output.isEmpty()) {
			return List.of();
		}
		
		return List.of(output.get());
	}
	
}
