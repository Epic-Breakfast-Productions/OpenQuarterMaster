package tech.ebp.oqm.lib.core.object.service.roles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import tech.ebp.oqm.lib.core.validation.annotations.ValidServiceRole;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestedRole {
	
	@NonNull
	@NotNull
	@NotBlank
	@ValidServiceRole
	public String role;
	
	@NonNull
	@NotNull
	@NotBlank
	@ValidServiceRole
	private String reason;
	
	private boolean optional = true;
}
