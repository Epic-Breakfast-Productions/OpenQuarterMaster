package tech.ebp.oqm.baseStation.model.rest.auth.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * The request object for logging in a user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest {
	
	@NonNull
	@NotNull
	@NotBlank
	private String usernameEmail;
	@NonNull
	@NotNull
	@NotBlank
	private String password;
	@NotNull
	private boolean extendedExpire = false;
}
