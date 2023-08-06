package tech.ebp.oqm.baseStation.model.object.interactingEntity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.ImagedMainObject;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntityType;
import tech.ebp.oqm.baseStation.model.rest.user.UserCreateRequest;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidUserRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder(builderClassName = "Builder")
public class User extends InteractingEntity {
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String name;
	
	@NonNull
	@NotNull
	@NotBlank
	@Size(max = 30)
	private String username;
	
	@NonNull
	@NotNull
	@Email
	private String email;
	
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private NotificationSettings notificationSettings = new NotificationSettings();
	
	private Set<@ValidUserRole String> roles = new HashSet<>();
	
	/**
	 * Still responsible for setting:
	 * <ul>
	 *     <li>password hash</li>
	 *     <li>Roles</li>
	 *     <li>attributes</li>
	 * </ul>
	 *
	 * @param userCreateRequest
	 *
	 * @return
	 */
	public static User.Builder builder(UserCreateRequest userCreateRequest) {
		return new User.Builder()
				   .name(userCreateRequest.getFirstName())
				   .username(userCreateRequest.getUsername())
				   .email(userCreateRequest.getEmail());
	}
	
	public static User.Builder builder() {
		return new User.Builder();
	}
	
	@Override
	public InteractingEntityType getInteractingEntityType() {
		return InteractingEntityType.USER;
	}
}
