package tech.ebp.oqm.core.api.model.object.storage.items.stored;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordMainObject;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.ImagedMainObject;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

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
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "storedType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = AmountStored.class, name = "AMOUNT"),
	@JsonSubTypes.Type(value = UniqueStored.class, name = "UNIQUE")
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
public abstract class Stored extends ImagedMainObject implements FileAttachmentContaining {

	public abstract StoredType getStoredType();

	/**
	 * The {@link tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem} this stored is associated with.
	 */
	@NonNull
	@NotNull
	private ObjectId item;

	/**
	 * The {@link tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock} this stored is stored in.
	 */
	//TODO:: determine if we can use null for 'no block in particular'
	private ObjectId storageBlock;

	/**
	 * The barcode for this particular stored object.
	 * <p>
	 * TODO:: validate?
	 * TODO:: rework, support multiple
	 */
	private String barcode = null;

	/**
	 * When the item(s) held expire. Null if it does not expire.
	 */
	private LocalDateTime expires = null;

	/**
	 * Statuses about this stored object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private StoredNotificationStatus notificationStatus = new StoredNotificationStatus();

	/**
	 * The condition of the stored object. 100 = mint, 0 = completely deteriorated. Null if N/A.
	 */
	@Max(100)
	@Min(0)
	private Integer condition = null;

	/**
	 * Notes on the condition on the thing(s) stored.
	 */
	private String conditionNotes = null;

	/**
	 * List of images related to the object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<@NonNull ObjectId> imageIds = new ArrayList<>();

	@lombok.Builder.Default
	private Set<ObjectId> attachedFiles = new HashSet<>();

	public static Predicate<Stored> getHasIdPredicate(UUID storedId) {
		return (Stored stored) -> stored.getId().equals(storedId);
	}

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public abstract String getLabelText();
}
