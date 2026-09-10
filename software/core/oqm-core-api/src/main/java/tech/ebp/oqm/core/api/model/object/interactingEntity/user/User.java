package tech.ebp.oqm.core.api.model.object.interactingEntity.user;

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
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.core.api.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUserRole;
import tech.ebp.oqm.core.api.service.JwtUtils;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@BsonDiscriminator
@Schema(title = "User", description = "An air breathing human user.")
public class User extends InteractingEntity {

	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String name;

	@Nullable
//	@NonNull
//	@NotNull
//	@NotBlank
//	@Size(max = 30)
	@Pattern(regexp = ".*\\S.*")//not blank, allow null
	private String username;

	@Nullable
//	@NonNull
//	@NotNull
	@Email
	private String email;

	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Set<@ValidUserRole String> roles = new HashSet<>();

	@Override
	@Schema(constValue = "USER", readOnly = true, required = true, examples = "USER")
	public InteractingEntityType getType() {
		return InteractingEntityType.USER;
	}

	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		boolean updated = false;

		{//name
			String newName = JwtUtils.getName(jwt);
			if(!this.getName().equals(newName)) {
				this.setName(newName);
				updated = true;
			}
		}
		{//email
			String newEmail = JwtUtils.getEmail(jwt);
			if(this.getEmail() == null){
				if(newEmail != null) {
					this.setEmail(newEmail);
					updated = true;
				}
			} else if(!this.getEmail().equals(newEmail)) {
				this.setEmail(newEmail);
				updated = true;
			}
		}
		{//username
			String newUsername = JwtUtils.getUserName(jwt);
			if(this.getUsername() == null){
				if(newUsername != null) {
					this.setUsername(newUsername);
					updated = true;
				}
			} else if(!this.getUsername().equals(newUsername)) {
				this.setUsername(newUsername);
				updated = true;
			}
		}

		if(!this.getRoles().equals(JwtUtils.getRoles(jwt))){
			this.setRoles(JwtUtils.getRoles(jwt));
			updated = true;
		}

		return updated;
	}
}
