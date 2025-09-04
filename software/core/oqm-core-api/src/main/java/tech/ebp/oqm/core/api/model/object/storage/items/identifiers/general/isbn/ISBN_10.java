package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.general.isbn;

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
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidISBN10;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ISBN_10 extends GeneralId {
	
	@NonNull
	@NotNull
	@ValidISBN10
	private String value;
	
	@Override
	public GeneralIdType getType() {
		return GeneralIdType.ISBN_10;
	}
}
