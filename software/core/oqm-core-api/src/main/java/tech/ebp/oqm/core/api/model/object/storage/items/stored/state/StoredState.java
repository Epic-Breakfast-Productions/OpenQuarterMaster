package tech.ebp.oqm.core.api.model.object.storage.items.stored.state;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.InTransit;

@Data
@SuperBuilder(toBuilder = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = StoredInBlock.class, name = "STORED"),
	@JsonSubTypes.Type(value = InTransit.class, name = "IN_TRANSIT")
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(oneOf = {StoredInBlock.class, InTransit.class})
@BsonDiscriminator
@NoArgsConstructor
public abstract class StoredState {

	public abstract StoredStateType getType();
}
