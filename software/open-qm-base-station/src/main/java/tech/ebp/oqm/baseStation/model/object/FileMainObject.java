package tech.ebp.oqm.baseStation.model.object;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class FileMainObject
	extends AttKeywordMainObject
	//	implements AttKeywordContaining
{
	
	public FileMainObject(ObjectId id, Map<@NotBlank @NotNull String, String> attributes, List<@NotBlank String> keywords) {
		super(id, attributes, keywords);
	}
	
	@NotNull
	@NonNull
	@NotBlank
	private String fileName;
}
