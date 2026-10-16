package tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.AttKeywordContaining;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImageAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredState;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.StoredStateType;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.state.inTransit.locale.InTransitLocale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
//@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "InTransit", description = "The state to specify this stored is not yet stored, but in transit to another location.")
public class InTransit extends StoredState implements AttKeywordContaining, FileAttachmentContaining, ImageAttachmentContaining {

	@Override
	public StoredStateType getType() {
		return StoredStateType.IN_TRANSIT;
	}

	@NotNull
	@NonNull
	private InTransitLocale from;

	@Nullable
	private InTransitLocale to;


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


	/**
	 * List of images related to the object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Images to associate with this object.", examples = {"[]"})
	List<@NonNull ObjectId> imageIds = new ArrayList<>();

	/**
	 * Files that have been attached to the item.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Files to attach to the item.", examples = {"[]"})
	private Set<@NotNull ObjectId> attachedFiles = new LinkedHashSet<>();
}
