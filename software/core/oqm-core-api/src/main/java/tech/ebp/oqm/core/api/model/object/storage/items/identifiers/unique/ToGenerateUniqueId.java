package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ToGenerateUniqueId extends UniqueId {
	
	@lombok.Builder.Default
	private boolean barcode = false;
	
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
}
