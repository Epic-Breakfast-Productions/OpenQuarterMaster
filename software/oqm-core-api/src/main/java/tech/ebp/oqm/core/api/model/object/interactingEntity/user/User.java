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
		if(!this.getEmail().equals(JwtUtils.getEmail(jwt))){
			this.setEmail(JwtUtils.getEmail(jwt));
			updated = true;
		}
		if(!this.getName().equals(JwtUtils.getName(jwt))){
			this.setName(JwtUtils.getName(jwt));
			updated = true;
		}
		if(!this.getUsername().equals(JwtUtils.getUserName(jwt))){
			this.setName(JwtUtils.getName(jwt));
			updated = true;
		}
		if(!this.getRoles().equals(JwtUtils.getRoles(jwt))){
			this.setRoles(JwtUtils.getRoles(jwt));
			updated = true;
		}
		
		return updated;
	}
}
