package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.ean;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidEAN8;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class EAN_8 extends Identifier {
	
	@NonNull
	@NotNull
	@ValidEAN8
	private String value;
	
	@Override
	public IdentifierType getType() {
		return IdentifierType.EAN_8;
	}
}
