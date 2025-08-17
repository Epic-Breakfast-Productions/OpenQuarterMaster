package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;
import tech.ebp.oqm.core.api.model.object.storage.items.notification.StoredNotificationStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes an item stored in the system.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = AmountStored.class, name = "AMOUNT"),
	@JsonSubTypes.Type(value = UniqueStored.class, name = "UNIQUE")
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
@Schema(oneOf = {AmountStored.class, UniqueStored.class})
public abstract class Stored extends ImagedMainObject implements FileAttachmentContaining {
	
	public static final int CUR_SCHEMA_VERSION = 1;
	
	
	@Schema(required = true, description = "The type of stored object.")
	public abstract StoredType getType();
	
	/**
	 * The {@link tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem} this stored is associated with.
	 */
	@NonNull
	@NotNull
	@Schema(description = "The item that this stored is associated with.")
	private ObjectId item;
	
	/**
	 * The {@link tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock} this stored is stored in.
	 * TODO:: determine if we can use null for 'no block in particular'
	 */
	@Schema(required = true, description = "The storage block where this item is stored.")
	private ObjectId storageBlock;
	
	/**
	 * The barcode for this particular stored object.
	 * <p>
	 * TODO:: validate?
	 * TODO:: rework, support multiple
	 */
	@lombok.Builder.Default
	private String barcode = null;
	
	/**
	 * When the item(s) held expire. Null if it does not expire.
	 */
	@lombok.Builder.Default
	@Schema(required = false, description = "When the item(s) held expire. Null if it does not expire.", examples = {"null", "2022-03-10T12:15:50"})
	private LocalDateTime expires = null;
	
	/**
	 * Statuses about this stored object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "State of the notifications sent about this item stored.")
	private StoredNotificationStatus notificationStatus = new StoredNotificationStatus();
	
	/**
	 * The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.
	 */
	@Max(100)
	@Min(0)
	@lombok.Builder.Default
	@Schema(required = false, description = "The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.", examples = {"null", "100"})
	private Integer condition = null;
	
	/**
	 * Notes on the condition on the thing(s) stored.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	@Schema(required = false, description = "Notes on the condition on the thing(s) stored.", examples = {""})
	private String conditionNotes = "";
	
	@lombok.Builder.Default
	private Set<ObjectId> attachedFiles = new HashSet<>();
	
	/**
	 * A Generated label text.
	 *
	 * @return
	 */
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Schema(required = false, description = "A generated label text.")
	public abstract String getLabelText();
	
	
	@Override
	public int getSchemaVersion() {
		return CUR_SCHEMA_VERSION;
	}
}
