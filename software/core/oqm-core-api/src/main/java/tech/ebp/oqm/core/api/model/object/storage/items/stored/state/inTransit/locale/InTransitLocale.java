package tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;

@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
//@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = GenericLocale.class, name = "GENERIC"),
	@JsonSubTypes.Type(value = StorageBlockLocale.class, name = "STORAGE_BLOCK")
})
@Schema(oneOf = {GenericLocale.class, StorageBlockLocale.class})
public abstract class InTransitLocale {

	public abstract LocaleType getType();
}
