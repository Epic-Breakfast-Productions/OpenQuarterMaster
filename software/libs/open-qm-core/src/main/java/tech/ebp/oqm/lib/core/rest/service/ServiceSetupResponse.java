package tech.ebp.oqm.lib.core.rest.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ServiceSetupResponse {
	
	@NonNull
	@NotNull
	private String setupSecret;
	
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private List<String> grantedRoles = new ArrayList<>();
}
