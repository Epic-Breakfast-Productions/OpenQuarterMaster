package tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.checkedInBy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@Data
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CheckedInByOqmEntity.class, name = "OQM_ENTITY"),
	@JsonSubTypes.Type(value = CheckedInByExtUser.class, name = "EXT_SYS_USER")
})
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
public abstract class CheckedInBy {
	
	public abstract CheckedInByType getType();
}
