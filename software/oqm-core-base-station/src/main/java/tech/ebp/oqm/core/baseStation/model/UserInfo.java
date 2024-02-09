package tech.ebp.oqm.core.baseStation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
	private String id;
	private String name;
	private String username;
	private String email;
	private Set<String> roles;
}
