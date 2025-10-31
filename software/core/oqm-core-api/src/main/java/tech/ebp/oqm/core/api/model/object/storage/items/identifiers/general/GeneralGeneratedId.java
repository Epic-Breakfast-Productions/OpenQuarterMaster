package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Generated;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GeneralGeneratedId extends Generic implements Generated {
	
	@NonNull
	@NotNull
	private ObjectId generatedFrom;
	
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.GENERATED;
	}
}
