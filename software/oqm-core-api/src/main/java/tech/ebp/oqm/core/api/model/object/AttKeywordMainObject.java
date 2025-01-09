package tech.ebp.oqm.core.api.model.object;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AttKeywordMainObject
	extends MainObject
	//	implements AttKeywordContaining
{
	
	public AttKeywordMainObject(ObjectId id, Map<@NotBlank @NotNull String, String> attributes, List<@NotBlank String> keywords) {
		super(id);
		this.setAttributes(attributes);
		this.setKeywords(keywords);
	}
	
	/**
	 * Attributes this object might have, usable for any purpose.
	 */
	@NotNull
	@NonNull
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the object
	 */
	@NotNull
	@NonNull
	private List<@NotBlank String> keywords = new ArrayList<>();
	
}
