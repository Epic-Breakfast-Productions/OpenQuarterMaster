package tech.ebp.oqm.core.api.model.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public interface Versionable {
	
	@Schema(description = "The schema version of the object. NOT a version/ revision for the object itself, just the schema version.")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	int getSchemaVersion();
}
