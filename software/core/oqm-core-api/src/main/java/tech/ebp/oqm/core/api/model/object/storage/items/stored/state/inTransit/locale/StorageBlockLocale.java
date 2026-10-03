package tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale;


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
@Schema(title = "StorageBlockLocale", description = "A Location dealing with a storage block.")
public class StorageBlockLocale extends InTransitLocale {

	@NotNull
	@NonNull
	private ObjectId storageBlock;

	@Override
	@Schema(constValue = "STORAGE_BLOCK", readOnly = true, required = true, examples = "STORAGE_BLOCK")
	public LocaleType getType() {
		return LocaleType.STORAGE_BLOCK;
	}
}
