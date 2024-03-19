package tech.ebp.oqm.baseStation.model.object.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;

import java.util.List;
import java.util.Map;

/**
 * TODO:: refactor?
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder(builderClassName = "Builder")
public class Image extends FileMainObject {
	
	public Image(
		@NotNull @NonNull String fileName,
		@NotNull @NonNull @Size(max = 500) String description,
		@NotNull @NonNull @NotBlank @Size(max = 500) String source
	) {
		super(fileName, description, source);
	}
	
	public Image() {
		super();
	}
	
	public Image(
		ObjectId id,
		Map<@NotBlank @NotNull String, String> attributes,
		List<@NotBlank String> keywords
	) {
		super(id, attributes, keywords);
	}
}
