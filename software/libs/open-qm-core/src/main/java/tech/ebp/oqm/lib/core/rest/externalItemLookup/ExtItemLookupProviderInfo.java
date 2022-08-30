package tech.ebp.oqm.lib.core.rest.externalItemLookup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * Information about an external item lookup provider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtItemLookupProviderInfo {
	
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
