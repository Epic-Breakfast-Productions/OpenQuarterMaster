package com.ebp.openQuarterMaster.lib.core.rest.user;

import com.ebp.openQuarterMaster.lib.core.validation.annotations.ValidPassword;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * The response object from a user create request
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreateRequest {
	
	@NonNull
	@NotNull
	@NotBlank
	private String firstName;
	@NonNull
	@NotNull
	@NotBlank
	private String lastName;
	@NonNull
	@NotNull
	@NotBlank
	private String username;
	@NonNull
	@NotNull
	@Email
	private String email;
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String title = "";
	@NonNull
	@NotNull
	@ValidPassword
	private String password;
	@lombok.Builder.Default
	private Map<@NotBlank String, String> attributes = new HashMap<>();
}
