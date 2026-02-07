package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.upc;

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
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UPC_E extends Identifier {
	
	@NonNull
	@NotNull
	@ValidUPCE
	private String value;
	
	@Override
	public IdentifierType getType() {
		return IdentifierType.UPC_E;
	}
}
