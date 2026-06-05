package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Schema(title="UniquePlainStored", description = "Stored object to describe a single unique item.")
public class UniquePlainStored extends UniqueStored {

	@Schema(defaultValue = "UNIQUE")
	@Override
	public StoredType getType() {
		return StoredType.UNIQUE;
	}
}
