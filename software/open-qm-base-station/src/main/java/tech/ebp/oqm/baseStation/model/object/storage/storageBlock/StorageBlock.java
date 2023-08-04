package tech.ebp.oqm.baseStation.model.object.storage.storageBlock;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.HasParent;
import tech.ebp.oqm.baseStation.model.object.ImagedMainObject;

import javax.measure.Quantity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Describes an area for storage.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class StorageBlock extends ImagedMainObject implements HasParent {
	
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
	private String nickname = "";
	
	/**
	 * Text that describes the storage block
	 */
	@NonNull
	@NotNull
	private String description = "";
	
	/**
	 * The location of this storage block. If a sub-block, just the location within that sub-block.
	 */
	@NonNull
	@NotNull
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
	private List<@NotNull Quantity<?>> capacityMeasures = new ArrayList<>();
	
	/**
	 * Categories this storage block holds.
	 */
	@NonNull
	@NotNull
	private List<@NotNull ObjectId> storedCategories = new ArrayList<>();
	
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public String getLabelText() {
		if (this.getNickname().isBlank()) {
			return this.getLabel();
		}
		return this.getLabel() + " / " + this.getNickname();
	}
}
