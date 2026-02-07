package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.generated;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GeneratedIdentifier extends GenericIdentifier implements Generated {
	
	@NonNull
	@NotNull
	private ObjectId generatedFrom;
	
	@Override
	public IdentifierType getType() {
		return IdentifierType.GENERATED;
	}
}
