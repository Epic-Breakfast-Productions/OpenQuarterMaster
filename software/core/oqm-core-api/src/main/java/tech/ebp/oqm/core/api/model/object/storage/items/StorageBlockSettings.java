package tech.ebp.oqm.core.api.model.object.storage.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.StorageBlockNotificationStatus;

import javax.measure.Quantity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StorageBlockSettings {

	@NonNull
	@NotNull
	private ObjectId storageBlock;

	/**
	 * Notes about how the items are stored in the storage block, if relevant
	 */
	private String notes;

	/**
	 * The threshold of low stock for the entire object.
	 * <p>
	 * Null for no threshold, Quantity with compatible unit to set the threshold.
	 */
	@lombok.Builder.Default
	@Schema(required = false, description = "The threshold of low stock for the associated storage block. Null for no threshold. Unit must be compatible with item's.")
	private Quantity<?> lowStockThreshold = null;

	@BsonIgnore
	@JsonIgnore
	public boolean hasLowStockThreshold() {
		return this.lowStockThreshold != null;
	}

	@NonNull
	@NotNull
	@Builder.Default
	private StorageBlockNotificationStatus notificationStatus = new StorageBlockNotificationStatus();
}
