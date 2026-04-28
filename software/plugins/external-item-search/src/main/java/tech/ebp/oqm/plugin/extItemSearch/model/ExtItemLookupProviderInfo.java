package tech.ebp.oqm.plugin.extItemSearch.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupSource;

import java.net.URI;
import java.util.Collection;

/**
 * Information about an external item lookup provider
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ExtItemLookupProviderInfo implements Comparable<ExtItemLookupProviderInfo> {
	
	@NotNull
	@NonNull
	private LookupService id;
	
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
	private URI homepage;
	
	public Collection<LookupMethod> getLookupMethods() {
		return this.getId().supportedMethods;
	}
	
	public Collection<LookupSource> getLookupSources() {
		return this.getId().supportedSources;
	}
	
	private boolean enabled;
	
	@Override
	public int compareTo(ExtItemLookupProviderInfo other) {
		return Comparator.INSTANCE.compare(this, other);
	}
	
	public static class Comparator implements java.util.Comparator<ExtItemLookupProviderInfo> {
		
		public static final Comparator INSTANCE = new Comparator();
		
		@Override
		public int compare(ExtItemLookupProviderInfo one, ExtItemLookupProviderInfo two) {
			if(one == two) {return 0;}
			if(one == null) {return 1;}
			if(two == null) {return -1;}
			int enabledResult = Boolean.compare(two.isEnabled(), one.isEnabled());
			if (enabledResult != 0) {
				return enabledResult;
			}
			return one.getDisplayName().compareTo(two.getDisplayName());
		}
	}
}
