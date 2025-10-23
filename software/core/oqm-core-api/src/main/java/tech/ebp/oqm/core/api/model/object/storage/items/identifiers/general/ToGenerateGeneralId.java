package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.Generates;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation.ToGenerate;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique.UniqueIdType;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ToGenerateGeneralId extends GeneralId implements ToGenerate {
	
	@NonNull
	@NotNull
	private ObjectId generateFrom;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.TO_GENERATE;
	}
	
	@Override
	public String getValue() {
		return null;
	}
	
	@Override
	public Generates generates() {
		return Generates.GENERAL;
	}
}
