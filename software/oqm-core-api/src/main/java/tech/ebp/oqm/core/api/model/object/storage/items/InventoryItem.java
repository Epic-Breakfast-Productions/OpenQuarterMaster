package tech.ebp.oqm.core.api.model.object.storage.items;

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
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidItemUnit;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUnit;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes a type of inventory item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@ValidItemUnit
public class InventoryItem extends ImagedMainObject implements FileAttachmentContaining {

	/**
	 * The name of this inventory item
	 */
	@NonNull
	@NotNull
	@NotBlank(message = "Name cannot be blank")
	private String name;

	/**
	 * The type of storage this item uses.
	 */
	@NonNull
	@NotNull
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private StorageType storageType;

	/**
	 * Description of the item
	 */
	@NonNull
	@NotNull
	private String description = "";

	/**
	 * The barcode for this item.
	 * <p>
	 * TODO:: validate?
	 * TODO:: rework
	 */
	private String barcode = null;

	/**
	 * Categories this item belongs to.
	 */
	@NonNull
	@NotNull
	private List<@NotNull ObjectId> categories = new ArrayList<>();

	/**
	 * The map of where the items are stored.
	 * <p>
	 * The key is the id of the storage block being stored in, the value the storage wrapper actually holding stored item information.
	 * <p>
	 * "new ObjectId(new byte[12])"/"000000000000000000000000" key is intended as a 'not stored anywhere in particular'
	 */
	@NonNull
	@NotNull
	private Set<ObjectId> storageBlocks = new LinkedHashSet<>();

	/**
	 * Files that have been attached to the item.
	 */
	@NonNull
	@NotNull
	private Set<@NotNull ObjectId> attachedFiles = new LinkedHashSet<>();

	private ItemNotificationStatus notificationStatus = new ItemNotificationStatus();

	/**
	 * When before a stored item expired to send a warning out about that expiration.
	 * <p>
	 * {@link Duration#ZERO} for no expiration.
	 */
	@NonNull
	@NotNull
	private Duration expiryWarningThreshold = Duration.ZERO;

	/**
	 * The threshold of low stock for the entire object.
	 * <p>
	 * Null for no threshold, Quantity with compatible unit to set the threshold.
	 * TODO:: validate unit is compatible with main unit
	 */
	private Quantity<?> lowStockThreshold = null;

	/**
	 * The unit to associate with this item. Stored items can have different units, but must be compatible with this one.
	 * <p>
	 * If using a storage type that uses the unique type, this must be a unit compatible with the 'unit' unit.
	 */
	@NonNull
	@NotNull
	@ValidUnit
	public Unit<?> unit;

	@Override
	public int getSchemaVersion() {
		return 2;
	}
}
