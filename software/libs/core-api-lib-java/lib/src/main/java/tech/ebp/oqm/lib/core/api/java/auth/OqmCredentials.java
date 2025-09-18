package tech.ebp.oqm.lib.core.api.java.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
//@NoArgsConstructor
public abstract class OqmCredentials {
	
	public abstract String getAccessHeaderContent();
}
