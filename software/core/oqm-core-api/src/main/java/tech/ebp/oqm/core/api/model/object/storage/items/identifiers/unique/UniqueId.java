package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = GeneratedUniqueId.class, name = "GENERATED"),
	@JsonSubTypes.Type(value = ProvidedUniqueId.class, name = "PROVIDED"),
	@JsonSubTypes.Type(value = ToGenerateUniqueId.class, name = "TO_GENERATE"),
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
public abstract class UniqueId extends Identifier {
	
	@lombok.Builder.Default
	private boolean useInLabel = false;
	
	public abstract UniqueIdType getType();
}
