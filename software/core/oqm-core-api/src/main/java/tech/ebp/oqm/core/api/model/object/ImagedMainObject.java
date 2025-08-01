package tech.ebp.oqm.core.api.model.object;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a main object that has images associated with it
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
public abstract class ImagedMainObject extends AttKeywordMainObject {
	
	/**
	 * List of images related to the object.
	 */
	@NonNull
	@NotNull
	@lombok.Builder.Default
	List<@NonNull ObjectId> imageIds = new ArrayList<>();
}
