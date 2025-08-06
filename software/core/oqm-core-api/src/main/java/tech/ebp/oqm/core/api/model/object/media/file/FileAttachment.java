package tech.ebp.oqm.core.api.model.object.media.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.core.api.model.object.FileMainObject;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileAttachment extends FileMainObject {
	
	public FileAttachment(
		@NotNull @NonNull String fileName,
		@NotNull @NonNull @Size(max = 500) String description,
		@NotNull @NonNull @NotBlank @Size(max = 500) String source
	) {
		super(fileName, description, source);
	}
	
	public FileAttachment() {
		super();
	}
}
