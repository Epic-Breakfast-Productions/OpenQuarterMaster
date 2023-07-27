package tech.ebp.oqm.baseStation.model.object.interactingEntity.externalService.roles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidServiceRole;

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
