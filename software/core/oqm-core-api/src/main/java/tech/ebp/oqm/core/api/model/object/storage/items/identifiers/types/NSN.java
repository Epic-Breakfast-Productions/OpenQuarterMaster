package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCA;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class NSN extends Identifier {

	@NonNull
	@NotNull
	@ValidUPCA
	private String value;

	@Override
	public IdentifierType getType() {
		return IdentifierType.NSN;
	}
}
