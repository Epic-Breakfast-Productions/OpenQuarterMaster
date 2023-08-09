package tech.ebp.oqm.baseStation.model.rest.externalItemLookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupResult;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
