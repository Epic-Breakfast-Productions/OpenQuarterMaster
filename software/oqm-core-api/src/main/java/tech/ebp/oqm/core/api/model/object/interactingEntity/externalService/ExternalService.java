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
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidServiceRole;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class ExternalService extends InteractingEntity {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 100)
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
	
	//TODO:: do a updater from
	
}
