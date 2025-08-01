package tech.ebp.oqm.core.api.model.object;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
@SuperBuilder(toBuilder = true)
public abstract class MainObject implements Versionable {
	
	/**
	 * The id of this object in the Mongodb.
	 * TODO:: make a string in openapi docs
	 */
	@Schema(
		description = "ObjectId hex string",
		example = ""
	)
	private ObjectId id;
}
