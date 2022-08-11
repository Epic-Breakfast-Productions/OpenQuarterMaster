package com.ebp.openQuarterMaster.lib.core.rest.productLookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ProductLookupResult {
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
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Map<String, String> attributes = new HashMap<>();
}
