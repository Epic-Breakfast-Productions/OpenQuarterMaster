package tech.ebp.oqm.lib.core.object.media.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.Binary;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;
import tech.ebp.oqm.lib.core.object.media.file.FileHashes;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO:: use gridfs:
 *   - https://www.mongodb.com/docs/drivers/java/sync/v4.3/fundamentals/gridfs/
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder(builderClassName = "Builder")
public class FileAttachment extends AttKeywordMainObject {
	
	@NotNull
	@NonNull
	@NotBlank
	private String fileName;
	
	@NotNull
	@NonNull
	@NotBlank
	private String mimeType;
	
	@NotNull
	@NonNull
	private FileHashes hashes;
	
	@NotNull
	@NonNull
	private Binary data;
}
