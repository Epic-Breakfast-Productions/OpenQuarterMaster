package tech.ebp.oqm.baseStation.model.rest.externalItemLookup;

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
public class ExtItemLookupProviderInfo implements Comparable<ExtItemLookupProviderInfo> {
	
	@NotNull
	@NonNull
	private String displayName;
	
	@NotNull
	@NonNull
	@Builder.Default
	private String description = "";
	
	@NotNull
	@NonNull
	private String cost;
	
	@Builder.Default
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
