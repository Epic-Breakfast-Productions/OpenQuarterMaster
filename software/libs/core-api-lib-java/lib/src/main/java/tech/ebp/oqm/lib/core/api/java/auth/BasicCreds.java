package tech.ebp.oqm.lib.core.api.java.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Base64;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public class BasicCreds extends OqmCredentials {
	
	private String name;
	private String password;
	
	@Override
	public String getAccessHeaderContent() {
		return "Authorization: Basic " + Base64.getEncoder().encodeToString((this.getName() + ":" + this.getPassword()).getBytes());
	}
}
