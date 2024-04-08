package tech.ebp.oqm.core.api.service.serviceState.db;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OqmMongoDatabase extends MainObject {
	
	@NotNull
	@Length(min = 1, max = 15)
	@Pattern(regexp = "^([A-Z]|[a-z]|[0-9]|[-_])+$")//TODO:: test
	private String name;
	
	@NotNull
	@Length(max = 256)
	private String description = "";
	
	private Set<@NotNull String> usersAllowed = null;
}
