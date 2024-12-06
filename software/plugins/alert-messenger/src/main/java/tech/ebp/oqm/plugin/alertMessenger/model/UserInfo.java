package tech.ebp.oqm.plugin.alertMessenger.model;

import java.util.HashSet;
/*import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;*/
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

// Represents user information stored in the database, including roles and contact details.
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserInfo {
    @Id
    private UUID id;

    private String name;

    private String username;

    private String email;

	@ElementCollection
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "role")
	
	// Stores user roles as a set of strings linked to the user in a separate table.
	private Set<String> roles = new HashSet<>(); // Initialize with a modifiable collection

	// Getter
	public Set<String> getRoles() {
		return roles;
	}

	// Setter
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	// Custom method to match the expected getUserId() in your other code
	public String getUserId() {
		return id.toString();
	}
}
