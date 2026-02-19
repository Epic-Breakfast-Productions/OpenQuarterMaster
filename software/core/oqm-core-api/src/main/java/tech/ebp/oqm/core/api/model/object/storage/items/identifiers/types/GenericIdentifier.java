package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GenericIdentifier extends Identifier {
	
	@NotBlank
	private String value;
	
	@lombok.Builder.Default
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private boolean barcode = false;
	
	@Override
	public IdentifierType getType() {
		return IdentifierType.GENERIC;
	}
}
