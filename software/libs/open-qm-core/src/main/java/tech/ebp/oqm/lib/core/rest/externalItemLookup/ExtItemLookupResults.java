package tech.ebp.oqm.lib.core.rest.externalItemLookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ExtItemLookupResults {
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private List<ExtItemLookupResult> results = new ArrayList<>();
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Map<@NonNull @NotNull @NotBlank String, String> serviceErrs = new HashMap<>();
}
