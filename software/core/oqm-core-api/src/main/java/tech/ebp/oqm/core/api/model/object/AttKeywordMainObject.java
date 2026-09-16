package tech.ebp.oqm.core.api.model.object;

import com.fasterxml.jackson.annotation.JsonMerge;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
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
//	@JsonMerge TODO:: #1261 figure out how to merge these; without, the whole map gets written on merge. With, partial updates are possible, but can't remove entries.
	@lombok.Builder.Default
	@Schema(required = false, description = "Attribute key/value (string) pairs to associate with the object.", examples = {"{}", "{\"key\": \"value\"}"})
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the object
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Keywords to associate with the object.", examples = {"[]", "[\"keyword\"]"})
	private List<@NotBlank String> keywords = new ArrayList<>();
	
}
