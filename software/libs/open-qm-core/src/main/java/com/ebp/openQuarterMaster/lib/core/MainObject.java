package com.ebp.openQuarterMaster.lib.core;

import com.ebp.openQuarterMaster.lib.core.history.Historied;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
public class MainObject extends Historied {
	/**
	 * The id of this object in the Mongodb.
	 */
	@Schema(example = "ObjectId hex string")
	private ObjectId id;
	/**
	 * Attributes this object might have, usable for any purpose.
	 */
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the object
	 */
	@NotNull
	@NonNull
	private List<@NotBlank String> keywords = new ArrayList<>();
}
