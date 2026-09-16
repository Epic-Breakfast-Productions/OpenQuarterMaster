package tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.AttKeywordContaining;
import tech.ebp.oqm.core.api.model.object.FileAttachmentContaining;
import tech.ebp.oqm.core.api.model.object.storage.checkout.CheckInType;
import tech.ebp.oqm.core.api.model.object.storage.checkout.checkinDetails.checkedInBy.CheckedInBy;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ReturnFullCheckinDetails.class, name = "RETURN_FULL"),
	@JsonSubTypes.Type(value = ReturnPartCheckinDetails.class, name = "RETURN_PART"),
	@JsonSubTypes.Type(value = LossCheckinDetails.class, name = "LOSS"),
})
@BsonDiscriminator
@SuperBuilder(toBuilder = true)
public abstract class CheckInDetails implements AttKeywordContaining, FileAttachmentContaining {
	
	public abstract CheckInType getType();
	
	/**
	 * Notes about the checkin
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private String notes = "";
	
	/**
	 * When the checkin took place
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	private ZonedDateTime checkinDateTime = ZonedDateTime.now();
	
	/**
	 * List of images related to the checkin.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<@NonNull ObjectId> imageIds = new ArrayList<>();
	
	/**
	 * List of files related to the checkin.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	Set<@NonNull ObjectId> attachedFiles = new HashSet<>();
	
	/**
	 * Attributes this object might have, usable for any purpose.
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the object
	 */
	@NotNull
	@NonNull
	@lombok.Builder.Default
	private List<@NotBlank String> keywords = new ArrayList<>();
	
	@NotNull
	@NonNull
	private CheckedInBy checkedInBy;
}
