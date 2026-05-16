package tech.ebp.oqm.core.api.model.object.storage.items.stored.state;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.AmountStored;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.UniqueStored;

@Data
@SuperBuilder(toBuilder = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = StoredInBlock.class, name = "STORED")
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(oneOf = {AmountStored.class, UniqueStored.class})
@BsonDiscriminator
@NoArgsConstructor
public abstract class StoredState {
	
	public abstract StoredStateType getType();
}
