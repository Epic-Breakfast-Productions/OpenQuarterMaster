package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;

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

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ToGenerateUniqueId extends UniqueId implements ToGenerate {
	
	@NonNull
	@NotNull
	private ObjectId generateFrom;
	
	@Override
	public UniqueIdType getType() {
		return UniqueIdType.TO_GENERATE;
	}
	
	@Override
	public String getValue() {
		return null;
	}
	
	@Override
	public boolean isBarcode() {
		return false;
	}
	
	@Override
	public Generates generates() {
		return Generates.UNIQUE;
	}
}
