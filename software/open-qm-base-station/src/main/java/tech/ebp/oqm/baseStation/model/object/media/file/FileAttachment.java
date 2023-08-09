package tech.ebp.oqm.baseStation.model.object.media.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;

import jakarta.validation.constraints.NotNull;

/**
 * TODO:: use gridfs:
 *   - https://www.mongodb.com/docs/drivers/java/sync/v4.3/fundamentals/gridfs/
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileAttachment extends FileMainObject {
	@NotNull
	@NonNull
	private String description;
}
