package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCA;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class Generic extends GeneralId {
	
	@NotBlank
	private String value;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.GENERIC;
	}
}
