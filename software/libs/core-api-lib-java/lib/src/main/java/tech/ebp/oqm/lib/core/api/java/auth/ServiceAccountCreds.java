package tech.ebp.oqm.lib.core.api.java.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class ServiceAccountCreds extends OqmCredentials {
	
	private String name;
	private String key;
	
	
	
	
	
	
	
	@Override
	public String getAccessHeaderContent() {
		return "";
	}
}
