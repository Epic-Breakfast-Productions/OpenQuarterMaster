package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.upc;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralIdType;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCA;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidUPCE;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UPC_E extends GeneralId {
	
	@NonNull
	@NotNull
	@ValidUPCE
	private String value;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.UPC_E;
	}
}
