package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UniqueStored extends Stored {
	
	@Override
	public StoredType getType() {
		return StoredType.UNIQUE;
	}
	
	@Override
	public String getDefaultLabelFormat() {
		return "{id}";
	}
}
