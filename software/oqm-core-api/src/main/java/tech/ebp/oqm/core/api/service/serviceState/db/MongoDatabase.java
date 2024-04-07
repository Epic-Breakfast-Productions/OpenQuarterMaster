package tech.ebp.oqm.core.api.service.serviceState.db;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MongoDatabase extends MainObject {
	
	@NotNull
	private String name;
	
	private Set<@NotNull String> usersAllowed = null;
}
