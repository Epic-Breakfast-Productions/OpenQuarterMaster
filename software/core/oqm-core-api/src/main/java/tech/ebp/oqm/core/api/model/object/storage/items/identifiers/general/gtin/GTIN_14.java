package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.gtin;

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
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidGTIN14;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidISBN13;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class GTIN_14 extends GeneralId {
	
	@NonNull
	@NotNull
	@ValidGTIN14
	private String value;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.GTIN_14;
	}
}
