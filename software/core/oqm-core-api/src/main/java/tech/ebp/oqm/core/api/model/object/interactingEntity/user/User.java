package tech.ebp.oqm.core.api.model.object.interactingEntity.user;

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
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.eclipse.microprofile.jwt.JsonWebToken;
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
public class User extends InteractingEntity {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String name;
	
//	@NonNull
//	@NotNull
//	@NotBlank
//	@Size(max = 30)
	private String username;
	
//	@NonNull
//	@NotNull
	@Email
	private String email;
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private NotificationSettings notificationSettings = new NotificationSettings();


	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Set<@ValidUserRole String> roles = new HashSet<>();
	
	@Override
	public InteractingEntityType getType() {
		return InteractingEntityType.USER;
	}
	
	@Override
	public boolean updateFrom(JsonWebToken jwt) {
		boolean updated = false;
		String jwtEmail = JwtUtils.getEmail(jwt);
		if (!JwtUtils.safeEquals(this.getEmail(), jwtEmail)) {
			this.setEmail(jwtEmail);
			updated = true;
		}
		String jwtName = JwtUtils.getName(jwt);
		if (!JwtUtils.safeEquals(this.getName(), jwtName)) {
			this.setName(jwtName);
			updated = true;
		}
		String jwtUsername = JwtUtils.getUserName(jwt);
		if (!JwtUtils.safeEquals(this.getUsername(), jwtUsername)) {
			this.setUsername(jwtUsername);
			updated = true;
		}
		Set<String> jwtRoles = JwtUtils.getRoles(jwt);
		if (!JwtUtils.safeEquals(this.getRoles(), jwtRoles)) {
			this.setRoles(jwtRoles);
			updated = true;
		}

		return updated;
	}
}
