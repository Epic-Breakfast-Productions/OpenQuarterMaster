package tech.ebp.oqm.core.api.model.object.storage.items.identifiers;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class Identifier {
	
	public abstract String getValue();
	public abstract boolean isBarcode();
}
