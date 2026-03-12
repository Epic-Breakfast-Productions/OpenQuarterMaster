package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.isbn;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.IdentifierType;
import tech.ebp.oqm.core.api.model.validation.annotations.identifiers.ValidISBN10;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ISBN_10 extends Identifier {
	
	@NonNull
	@NotNull
	@ValidISBN10
	private String value;
	
	@Override
	public IdentifierType getType() {
		return IdentifierType.ISBN_10;
	}
}
