package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Generic extends GeneralId {
	
	@NotBlank
	private String value;
	
	@JsonProperty(access = JsonProperty.Access.READ_WRITE)
	private boolean barcode = false;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.GENERIC;
	}
}
