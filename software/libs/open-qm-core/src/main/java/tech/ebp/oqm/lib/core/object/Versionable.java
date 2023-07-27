package tech.ebp.oqm.lib.core.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Versionable {
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public int getObjectVersion();
}
