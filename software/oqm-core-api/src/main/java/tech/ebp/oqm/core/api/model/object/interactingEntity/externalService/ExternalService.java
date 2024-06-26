package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.rest.externalService.ExternalServiceSetupRequest;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidServiceRole;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class ExternalService extends InteractingEntity {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 100)
	private String name;
	
	@NonNull
	@NotNull
	@NotBlank
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
	private Set<@ValidServiceRole String> roles = new HashSet<>();
	
	/**
	 * Only used when authmode == SELF
	 */
	private String setupTokenHash;
	
	/**
	 * Wrapper for {@link #getDeveloperEmail()}
	 *
	 * @return
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Override
	public String getEmail() {
		return this.getDeveloperEmail();
	}
	
	public boolean changedGiven(ExternalServiceSetupRequest newServiceIn) {
		if (!this.getName().equals(newServiceIn.getName())) {
			return true;
		}
		if (!this.getDescription().equals(newServiceIn.getDescription())) {
			return true;
		}
		if (!this.getDeveloperEmail().equals(newServiceIn.getDeveloperEmail())) {
			return true;
		}
		if (!this.getDeveloperName().equals(newServiceIn.getDeveloperName())) {
			return true;
		}
		return false;
	}
	
	//TODO:: do a updater from
	
}
