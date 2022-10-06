package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.measure.Quantity;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class SingleStoredWrapper<T extends Stored> extends StoredWrapper<T, T> {
	
	protected SingleStoredWrapper(T stored) {
		super(stored);
	}
	
	@Override
	public long getNumStored() {
		return 1;
	}
	
}
