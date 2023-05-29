package tech.ebp.oqm.lib.core.object.storage.checkout.checkoutFor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CheckoutForOqmUser.class, name = "OQM_USER"),
	@JsonSubTypes.Type(value = CheckoutForExtUser.class, name = "EXT_SYS_USER")
})
public abstract class CheckoutFor {
	
	@NonNull
	@NotNull
	private String reason = "";
	
	public abstract CheckoutForType getType();
}
