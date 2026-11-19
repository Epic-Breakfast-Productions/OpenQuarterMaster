package tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "GenericLocale", description = "A generic location.")
public class GenericLocale extends InTransitLocale {

	@NotNull
	@NonNull
	@NotBlank
	private String name;
	private String description;

	@Override
	@Schema(constValue = "GENERIC", readOnly = true, required = true, examples = "GENERIC")
	public LocaleType getType() {
		return LocaleType.GENERIC;
	}
}
