package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.gtin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidGTIN14;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GTIN_14 extends Identifier {
	
	@NonNull
	@NotNull
	@ValidGTIN14
	private String value;
	
	@Override
	public IdentifierType getType() {
		return IdentifierType.GTIN_14;
	}
}
