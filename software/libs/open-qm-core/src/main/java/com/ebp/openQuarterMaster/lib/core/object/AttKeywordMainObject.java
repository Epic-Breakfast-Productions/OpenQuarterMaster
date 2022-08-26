package com.ebp.openQuarterMaster.lib.core.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
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
