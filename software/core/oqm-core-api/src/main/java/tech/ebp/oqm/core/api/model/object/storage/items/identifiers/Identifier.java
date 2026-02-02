package tech.ebp.oqm.core.api.model.object.storage.items.identifiers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.Labeled;

@Data
@SuperBuilder(toBuilder = true)
//@AllArgsConstructor
@NoArgsConstructor
public abstract class Identifier implements Labeled {
	
	@lombok.Builder.Default
	private String label = null;
	
	public abstract String getValue();
	public abstract boolean isBarcode();
}
