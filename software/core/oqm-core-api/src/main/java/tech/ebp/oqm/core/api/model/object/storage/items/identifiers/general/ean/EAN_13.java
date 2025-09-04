package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.ean;

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
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidEAN13;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidEAN8;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidISBN13;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class EAN_13 extends GeneralId {
	
	@NonNull
	@NotNull
	@ValidEAN13
	private String value;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.EAN_13;
	}
}
