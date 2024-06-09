package tech.ebp.openQuarterMaster.plugin.productSearch.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.net.URL;

/**
 * Information about an external item lookup provider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtItemLookupProviderInfo implements Comparable<ExtItemLookupProviderInfo> {
	
	@NotNull
	@NonNull
	private String displayName;
	
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private String description = "";
	
	@NotNull
	@NonNull
	private String cost;

	@lombok.Builder.Default
	private boolean acceptsContributions = false;
	
	@NotNull
	@NonNull
	private URL homepage;
	
	private boolean enabled;
	
	@Override
	public int compareTo(ExtItemLookupProviderInfo other) {
		return Comparator.INSTANCE.compare(this, other);
	}
	
	public static class Comparator implements java.util.Comparator<ExtItemLookupProviderInfo> {
		
		public static final Comparator INSTANCE = new Comparator();
		
		@Override
		public int compare(ExtItemLookupProviderInfo one, ExtItemLookupProviderInfo two) {
			int enabledResult = Boolean.compare(two.isEnabled(), one.isEnabled());
			if (enabledResult != 0) {
				return enabledResult;
			}
			return one.getDisplayName().compareTo(two.getDisplayName());
		}
	}
}
