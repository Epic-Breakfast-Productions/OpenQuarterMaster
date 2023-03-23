package tech.ebp.oqm.lib.core.rest.externalItemLookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An individual result from external sources.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ExtItemLookupResult {
	@NonNull
	@NotNull
	@NotBlank
	private String source;
	
	private String brand;
	
	private String name;
	@NonNull
	@NotNull
	@NotBlank
	private String unifiedName;
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String description = "";
	
	private BigDecimal price;
	
	private String barcode;
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Map<String, String> attributes = new HashMap<>();
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private List<@NotNull @NonNull @NotBlank String> images = new ArrayList<>();
	
}
