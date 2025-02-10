package tech.ebp.oqm.core.api.model.object.storage.storageBlock;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.HasParent;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;

import javax.measure.Quantity;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes an area for storage.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class StorageBlock extends ImagedMainObject implements HasParent, FileAttachmentContaining {
	public static final int CUR_SCHEMA_VERSION = 1;

	/**
	 * The label for this storage block
	 */
	@NonNull
	@NotNull
	@NotBlank
	private String label;
	
	/**
	 * The nickname for this storage block
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String nickname = "";
	
	/**
	 * Text that describes the storage block
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String description = "";
	
	/**
	 * The location of this storage block. If a sub-block, just the location within that sub-block.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String location = "";
	
	/**
	 * The parent of this storage block, if any
	 */
	private ObjectId parent;
	
	/**
	 * The capacities of this storage block. Intended to describe different units of capacity for the block.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private List<@NotNull Quantity<?>> capacityMeasures = new ArrayList<>();
	
	/**
	 * Categories this storage block holds.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private List<@NotNull ObjectId> storedCategories = new ArrayList<>();
	
	/**
	 * Files that have been attached to the item.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private Set<@NotNull ObjectId> attachedFiles = new LinkedHashSet<>();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getLabelText() {
		if (this.getNickname().isBlank()) {
			return this.getLabel();
		}
		return this.getLabel() + " / " + this.getNickname();
	}

	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}

}
