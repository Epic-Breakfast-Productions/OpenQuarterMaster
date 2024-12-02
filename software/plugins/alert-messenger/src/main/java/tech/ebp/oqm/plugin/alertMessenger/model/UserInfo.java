package tech.ebp.oqm.plugin.alertMessenger.model;

/*import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;*/
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

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
    private Set<String> roles;

	// Custom method to match the expected getUserId() in your other code
	public String getUserId() {
		return id.toString();
	}
}

/*@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity // Makes this class a database entity
@Table(name = "users")
public class UserInfo {
	@Id // This field acts as the primary key
	private String id;

	private String name;
	private String username;
	private String email;
	private Set<String> roles;

	// Custom method to match the expected getUserId() in your other code
	public String getUserId() {
		return id;
	}
}*/
