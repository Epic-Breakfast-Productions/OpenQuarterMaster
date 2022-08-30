package tech.ebp.oqm.lib.core.object.user;

import tech.ebp.oqm.lib.core.object.ImagedMainObject;
import tech.ebp.oqm.lib.core.rest.user.UserCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderClassName = "Builder")
public class User extends ImagedMainObject {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String firstName;
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String lastName;
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String username;
	@NonNull
	@NotNull
	@Email
	private String email;
	private String title;
	
	/*
	 * Not used if service.authMode set to EXTERNAL
	 */
	private String pwHash = null;
	
	private Set<String> roles = new HashSet<>();
	
	/*
	 * Only used if service.authMode set to EXTERNAL
	 */
	private Map<@NotBlank String, String> externIds = new HashMap<>();
	
	/**
	 * Still responsible for setting:
	 * <ul>
	 *     <li>password hash</li>
	 *     <li>Roles</li>
	 *     <li>attributes</li>
	 * </ul>
	 *
	 * @param userCreateRequest
	 *
	 * @return
	 */
	public static Builder builder(UserCreateRequest userCreateRequest) {
		return new Builder()
			.firstName(userCreateRequest.getFirstName())
			.lastName(userCreateRequest.getLastName())
			.username(userCreateRequest.getUsername())
			.email(userCreateRequest.getEmail())
			.title(userCreateRequest.getTitle());
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	
}
