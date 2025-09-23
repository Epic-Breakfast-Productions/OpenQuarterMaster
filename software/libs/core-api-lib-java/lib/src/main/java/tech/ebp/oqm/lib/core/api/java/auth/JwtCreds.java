package tech.ebp.oqm.lib.core.api.java.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Base64;

/**
 * Credentials to provide a plain JWT for the client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Setter(AccessLevel.PRIVATE)
public class JwtCreds extends OqmCredentials {
	
	/**
	 * The actual JWT to use.
	 */
	private String jwt;
	
	@Override
	public String getAccessHeaderContent() {
		return "Bearer " + this.getJwt();
	}
}
