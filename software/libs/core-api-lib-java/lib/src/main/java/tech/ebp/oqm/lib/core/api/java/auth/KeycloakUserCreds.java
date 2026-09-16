package tech.ebp.oqm.lib.core.api.java.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Base64;

/**
 * Credentials to hold user information, and handle getting jwt credential, as well as getting that token.
 *
 * TODO:: this needs fleshed out when we know how we want to do this
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@Setter(AccessLevel.PRIVATE)
public class KeycloakUserCreds extends JwtCreds {
	
	/**
	 * The username? Needed?
	 */
	private String name;
	
	//TODO:: determine what would be useful to be here.
}
