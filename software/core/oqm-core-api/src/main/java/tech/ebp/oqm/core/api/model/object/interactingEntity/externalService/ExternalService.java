package tech.ebp.oqm.core.api.model.object.interactingEntity.externalService;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.validator.constraints.URL;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidServiceRole;
import tech.ebp.oqm.core.api.service.JwtUtils;

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

	@Nullable
	@Pattern(regexp = ".*\\S.*")//not blank, allow null
	private String developerName;

	@Nullable
	@Pattern(regexp = ".*\\S.*")//not blank, allow null
	@Email
	private String developerEmail;

	@Nullable
	@Pattern(regexp = ".*\\S.*")//not blank, allow null
	@URL
	private String developerWebsite;

	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Set<@ValidServiceRole String> roles = new HashSet<>();

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

	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		boolean updated = false;

		{//name
			String newName = JwtUtils.getServiceName(jwt);
			if(!this.getName().equals(newName)) {
				this.setName(newName);
				updated = true;
			}
		}

		{//dev name
			String newDevName = JwtUtils.getDevName(jwt);
			if(this.getDeveloperName() == null){
				if(newDevName != null) {
					this.setDeveloperName(newDevName);
					updated = true;
				}
			} else if(!this.getDeveloperName().equals(newDevName)) {
				this.setDeveloperName(newDevName);
				updated = true;
			}
		}

		{//dev email
			String newEmail = JwtUtils.getDevEmail(jwt);
			if(this.getDeveloperEmail() == null){
				if(newEmail != null) {
					this.setDeveloperEmail(newEmail);
					updated = true;
				}
			} else if(!this.getDeveloperEmail().equals(newEmail)) {
				this.setDeveloperEmail(newEmail);
				updated = true;
			}
		}

		{//dev website
			String newEmail = JwtUtils.getDevWebsite(jwt);
			if(this.getDeveloperWebsite() == null){
				if(newEmail != null) {
					this.setDeveloperWebsite(newEmail);
					updated = true;
				}
			} else if(!this.getDeveloperWebsite().equals(newEmail)) {
				this.setDeveloperWebsite(newEmail);
				updated = true;
			}
		}

		// roles
		if(!this.getRoles().equals(JwtUtils.getRoles(jwt))){
			this.setRoles(JwtUtils.getRoles(jwt));
			updated = true;
		}

		return updated;
	}
}
