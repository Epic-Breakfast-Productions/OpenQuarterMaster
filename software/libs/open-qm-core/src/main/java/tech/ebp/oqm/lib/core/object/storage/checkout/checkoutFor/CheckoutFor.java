package tech.ebp.oqm.lib.core.object.storage.checkout.checkoutFor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

@Data
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CheckoutForOqmEntity.class, name = "OQM_ENTITY"),
	@JsonSubTypes.Type(value = CheckoutForExtUser.class, name = "EXT_SYS_USER")
})
@BsonDiscriminator
public abstract class CheckoutFor {
	
	public abstract CheckoutForType getType();
}
