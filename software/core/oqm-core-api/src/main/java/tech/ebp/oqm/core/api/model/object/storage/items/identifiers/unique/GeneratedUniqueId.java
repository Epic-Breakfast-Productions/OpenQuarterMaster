package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
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
public class GeneratedUniqueId extends UniqueId implements Comparable<GeneratedUniqueId> {
	
	@NonNull
	@NotNull
	@NotBlank
	private String value;
	
	@lombok.Builder.Default
	private boolean barcode = false;
	
	@NonNull
	@NotNull
	private ObjectId generatedFrom;
	
	@Override
	public UniqueIdType getType() {
		return UniqueIdType.GENERATED;
	}
	
	@Override
	public int compareTo(GeneratedUniqueId generatedUniqueId) {
		return this.getValue().compareTo(generatedUniqueId.getValue());
	}
}
