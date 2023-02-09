package tech.ebp.oqm.lib.core.object.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
	
	public FileMetadata(File file) {
		this(
			file.getName(),
			file.length(),
			FileHashes.fromFile(file)
		);
	}
	
	
	@NonNull
	@NotNull
	@NotBlank
	private String origName;
	
	@Positive
	private long length;
	
	@NonNull
	@NotNull
	private FileHashes hashes;
}
