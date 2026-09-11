package tech.ebp.oqm.plugin.imageSearch.model.resnet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;

/**
 * Describes a single Resnet image vector based on an image.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageVector {

	/**
	 * Id from Mongodb
	 */
	private ObjectId id;

	/**
	 * The database this image is in
	 */
	@NonNull
	private String oqmDb;

	/**
	 * The id of the image
	 */
	@NonNull
	private String imageId;

	/**
	 * The version of this specific image
	 */
	private int imageRevision;

	/**
	 * The processed Resnet image data vector to use in comparisons
	 */
	private float[] vector;
}
