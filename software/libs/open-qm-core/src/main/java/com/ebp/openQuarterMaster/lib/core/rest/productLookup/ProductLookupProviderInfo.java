package com.ebp.openQuarterMaster.lib.core.rest.productLookup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.net.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductLookupProviderInfo {
	
	@NotNull
	@NonNull
	private String displayName;
	
	@NotNull
	@NonNull
	private String description;
	
	@NotNull
	@NonNull
	private String cost;
	
	private boolean acceptsContributions;
	
	@NotNull
	@NonNull
	private URL homepage;
	
	private boolean enabled;
}
