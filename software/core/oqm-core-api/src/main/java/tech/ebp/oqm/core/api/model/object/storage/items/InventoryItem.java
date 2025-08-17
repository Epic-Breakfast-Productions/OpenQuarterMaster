package tech.ebp.oqm.core.api.model.object.storage.items;

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
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.ItemNotificationStatus;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.stats.ItemStoredStats;
import tech.ebp.oqm.core.api.model.units.OqmProvidedUnits;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidItemUnit;
import tech.ebp.oqm.core.api.model.validation.annotations.ValidUnit;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
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
@Schema(description = "A type of item that is stored. Does not describe the exact items stored, but their type. Exact items stored are described by storeds.")
public class InventoryItem extends ImagedMainObject implements FileAttachmentContaining {
	public static final int CUR_SCHEMA_VERSION = 2;

	/**
	 * The name of this inventory item
	 */
	@NonNull
	@NotNull
	@NotBlank(message = "Name cannot be blank")
	@Schema(required = true, description = "The name of the item.", examples = {"Soap"})
	private String name;

	/**
	 * The type of storage this item uses.
	 */
	@NonNull
	@NotNull
	@Schema(required = true, description = "The type of storage this item uses.")
	private StorageType storageType;

	/**
	 * Description of the item
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "The description of this item.", examples = {""})
	private String description = "";

	/**
	 * The barcode for this item.
	 * <p>
	 * TODO:: validate?
	 * TODO:: rework
	 */
	@lombok.Builder.Default
	private String barcode = null;

	/**
	 * Categories this item belongs to.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "The categories this item belongs to.", examples = {"[]"})
	private Set<@NotNull ObjectId> categories = new HashSet<>();

	/**
	 * The map of where the items are stored.
	 * <p>
	 * The key is the id of the storage block being stored in, the value the storage wrapper actually holding stored item information.
	 * <p>
	 * "new ObjectId(new byte[12])"/"000000000000000000000000" key is intended as a 'not stored anywhere in particular'
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "The storage blocks this item is stored in.", examples = {"[]"})
	private LinkedHashSet<ObjectId> storageBlocks = new LinkedHashSet<>();

	/**
	 * Files that have been attached to the item.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Files to attach to the item.", examples = {"[]"})
		private Set<@NotNull ObjectId> attachedFiles = new LinkedHashSet<>();

	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "The state of notifications sent about item stored state (low stock, expiry)")
	private ItemNotificationStatus notificationStatus = new ItemNotificationStatus();

	/**
	 * When before a stored item expired to send a warning out about that expiration.
	 * <p>
	 * {@link Duration#ZERO} for no expiration.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "When before a stored item becomes expired to alert a warning about said expiration. Defaults to Zero.")
	private Duration expiryWarningThreshold = Duration.ZERO;

	/**
	 * The threshold of low stock for the entire object.
	 * <p>
	 * Null for no threshold, Quantity with compatible unit to set the threshold.
	 * TODO:: validate unit is compatible with main unit
	 */
	@lombok.Builder.Default
	@Schema(required = false, description = "The threshold of low stock for the entire item total. Null for no threshold. Unit must be compatible with item's.")
	private Quantity<?> lowStockThreshold = null;

	/**
	 * The stats for the stored items.
	 * <p>
	 * Null if a transaction was never performed on this item.
	 */
	@lombok.Builder.Default
	@Schema(required = false, description = "Stats about this item's stored instances.")
	private ItemStoredStats stats = null;

	/**
	 * The unit to associate with this item. Stored items can have different units, but must be compatible with this one.
	 * <p>
	 * If using a storage type that uses the amount type, this must be a unit compatible with the 'unit' unit.
	 */
	@NonNull
	@NotNull
	@ValidUnit
	@lombok.Builder.Default
	@Schema(required = false, description = "The unit to use to track these items. Check the compatible units endpoints to see what is available.")
	public Unit<?> unit = OqmProvidedUnits.UNIT;

	@Schema(defaultValue = InventoryItem.CUR_SCHEMA_VERSION+"")
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
