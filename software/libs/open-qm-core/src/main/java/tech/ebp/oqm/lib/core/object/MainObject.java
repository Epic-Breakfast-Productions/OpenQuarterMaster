package tech.ebp.oqm.lib.core.object;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public abstract class MainObject {
	
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
