package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class GeneralId extends Identifier {
	
	public abstract GeneralIdType getType();
	
	public abstract String getValue();
	
	public boolean isBarcode() {
		return this.getType().isBarcode;
	}
}
