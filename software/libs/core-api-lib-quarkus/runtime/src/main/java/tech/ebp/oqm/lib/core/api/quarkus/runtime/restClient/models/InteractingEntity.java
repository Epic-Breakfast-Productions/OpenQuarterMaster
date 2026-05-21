package tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractingEntity {
	private String id;
	private String idFromAuthProvider;
	private String authProvider;
	private String name;
	private String email;
	private String type;
	private Set<String> roles;
	private List<String> keywords;
	private Map<String, String> attributes;
}
