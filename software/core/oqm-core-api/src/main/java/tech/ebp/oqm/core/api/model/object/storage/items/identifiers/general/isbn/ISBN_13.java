package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralId;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.GeneralIdType;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidISBN13;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public class ISBN_13 extends GeneralId {
	
	@ValidISBN13
	private String value;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.ISBN_13;
	}
}
