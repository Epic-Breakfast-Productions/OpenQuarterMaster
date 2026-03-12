package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.generated;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ToGenerateIdentifier extends Identifier {
	
	@NonNull
	@NotNull
	private ObjectId generateFrom;
	
	@Override
	public IdentifierType getType() {
		return IdentifierType.TO_GENERATE;
	}
	
	@Override
	public String getValue() {
		return null;
	}
}
