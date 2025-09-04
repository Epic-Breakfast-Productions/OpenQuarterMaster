package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public abstract class UniqueId extends Identifier {
	
	@lombok.Builder.Default
	private boolean useInLabel = false;

}
