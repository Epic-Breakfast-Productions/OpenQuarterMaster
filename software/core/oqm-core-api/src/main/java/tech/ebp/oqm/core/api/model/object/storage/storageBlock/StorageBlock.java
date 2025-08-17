package tech.ebp.oqm.core.api.model.object.storage.storageBlock;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(name = "StorageBlock", description = "Describes where items can be stored.")
public class StorageBlock extends ImagedMainObject implements HasParent, FileAttachmentContaining {
	public static final int CUR_SCHEMA_VERSION = 1;

	/**
	 * The label for this storage block
	 */
	@NonNull
	@NotNull
	@NotBlank
	@Schema(required = true, description = "The label for this storage block", examples = {"Shed"})
	private String label;
	
	/**
	 * The nickname for this storage block.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "The nickname for this storage block.", examples = {"Junk Drawer"})
	private String nickname = "";
	
	/**
	 * Text that describes the storage block.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Text that describes the storage block.", examples = {""})
	private String description = "";
	
	/**
	 * The location of this storage block. If a sub-block, just the location within that sub-block.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = true, description = "The location of this storage block. If a sub-block, just the location within that sub-block.", examples = {""})
	private String location = "";
	
	/**
	 * The parent of this storage block, if any
	 */
	@Schema(required = false, description = "The parent storage block.", examples = {"null"})
	private ObjectId parent;
	
	/**
	 * The capacities of this storage block. Intended to describe different units of capacity for the block.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "The capacities of this storage block. Intended to describe different units of capacity for the block.", examples = {"null"})
	private List<@NotNull Quantity<?>> capacityMeasures = new ArrayList<>();
	
	/**
	 * The categories of items that are stored in this storage block.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "The categories of items that are stored in this storage block.", examples = {"[]"})
	private Set<@NotNull ObjectId> storedCategories = new LinkedHashSet<>();
	
	/**
	 * Files that have been attached to the item.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Files to attach to the storage block.", examples = {"[]"})
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
