package tech.ebp.oqm.core.api.model.rest.externalService;

//TODO:: for general, plugin

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.ServiceType;
import tech.ebp.oqm.core.api.model.object.interactingEntity.externalService.roles.RequestedRole;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "serviceType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = GeneralServiceSetupRequest.class, name = "GENERAL"),
	@JsonSubTypes.Type(value = PluginServiceSetupRequest.class, name = "PLUGIN"),
})
public abstract class ExternalServiceSetupRequest {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String name;
	
	@NonNull
	@NotNull
	@NotBlank
	@lombok.Builder.Default
	private String description = "";
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 50)
	private String developerName;
	
	@NonNull
	@NotNull
	@NotBlank
	@Email
	private String developerEmail;
	
	
	/**
	 * Only used when authmode == SELF
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Set<@Valid RequestedRole> requestedRoles = new HashSet<>();
	
	/**
	 * Only used when authmode == SELF
	 */
	private String secret;
	
	public abstract ServiceType getServiceType();
	
	public abstract ExternalService toExtService();
	
	protected void setCoreData(ExternalService externalService) {
		externalService
			.setName(this.getName())
			.setDescription(this.getDescription())
			.setDeveloperName(this.getDeveloperName())
			.setDeveloperEmail(this.getDeveloperEmail());
	}
}
