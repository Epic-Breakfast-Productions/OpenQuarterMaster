package tech.ebp.oqm.plugin.extItemSearch.model.lookupResult;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils.LookupType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ExtItemLookupResult.class, name = "SUCCESS"),
	@JsonSubTypes.Type(value = ExtItemLookupErrResult.class, name = "ERROR"),
	@JsonSubTypes.Type(value = LookupResultNoResults.class, name = "NO_RESULTS")
})
public abstract class LookupResult {
	
	public abstract ResultType getType();
	
	@NonNull
	@NotNull
	@NotBlank
	private String source;
	
	@NonNull
	@NotNull
	private LookupType lookupType;
}
