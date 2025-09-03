package tech.ebp.oqm.core.api.model.object.storage.items.identifiers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
//@AllArgsConstructor
@NoArgsConstructor
public abstract class Identifier {
	
	public abstract String getValue();
	public abstract boolean isBarcode();
}
