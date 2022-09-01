package tech.ebp.oqm.lib.core.rest.user;

import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;
import tech.ebp.oqm.lib.core.object.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The response object from getting a user
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserGetResponse extends AttKeywordMainObject {
	
	private ObjectId id;
	private String username;
	private String firstName;
	private String lastName;
	private String email;
	private String title;
	private boolean disabled;
	private Map<String, String> externIds = new HashMap<>();
	private Set<String> roles = new HashSet<>();
	private Map<String, String> attributes = new HashMap<>();
	
	public static Builder builder(User user) {
		return new Builder()
				   .id(user.getId())
				   .username(user.getUsername())
				   .firstName(user.getFirstName())
				   .lastName(user.getLastName())
				   .email(user.getEmail())
				   .title(user.getTitle())
				   .disabled(user.isDisabled())
				   .externIds(user.getExternIds())
				   .roles(user.getRoles())
				   .attributes(user.getAttributes());
	}
	
}
