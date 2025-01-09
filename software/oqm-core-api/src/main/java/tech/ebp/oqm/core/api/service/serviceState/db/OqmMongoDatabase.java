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
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.MainObject;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OqmMongoDatabase extends AttKeywordMainObject {
	public static final int CUR_SCHEMA_VERSION = 1;
	
	@NotNull
	@Length(min = 1, max = 15)
	@Pattern(regexp = "^([A-Z]|[a-z]|[0-9]|[-_])+$")//TODO:: test
	private String name;
	
	private String displayName = null;
	
	@NotNull
	@Length(max = 256)
	@lombok.Builder.Default
	private String description = "";
	
	private Set<@NotNull String> usersAllowed = null;
	
	public String getDisplayName(){
		if(this.displayName == null || this.displayName.isBlank()){
			return this.getName();
		}
		return this.displayName;
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
