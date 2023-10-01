package tech.ebp.oqm.baseStation.model.object.storage.items.stored;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Describes an item stored in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "storedType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = AmountStored.class, name = "AMOUNT"),
	@JsonSubTypes.Type(value = TrackedStored.class, name = "TRACKED")
})
@JsonInclude(JsonInclude.Include.ALWAYS)
@BsonDiscriminator
public abstract class Stored {
	
	public abstract StoredType getStoredType();
	
	/**
	 * The identifier used to identify this particular stored. Needs to be unique within a single inventory item.
	 */
	@NonNull
	@NotNull
	private UUID storedId = UUID.randomUUID();
	
	/**
	 * The barcode for this particular stored object.
	 * <p>
	 * TODO:: validate?
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
	List<@NonNull ObjectId> imageIds = new ArrayList<>();
	
	/**
	 * Attributes related to the item
	 */
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the item
	 */
	@NotNull
	@NonNull
	private List<@NotBlank String> keywords = new ArrayList<>();
	
	public static Predicate<Stored> getHasIdPredicate(UUID storedId) {
		return (Stored stored)->stored.getStoredId().equals(storedId);
	}
}
