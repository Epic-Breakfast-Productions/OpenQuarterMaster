package tech.ebp.oqm.baseStation.model.object.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
	
	public static final Tika TIKA = new Tika();
	
	public FileMetadata(File file) throws IOException {
		this(
			file.getName(),
			file.length(),
			FileHashes.fromFile(file),
			TIKA.detect(file),
			ZonedDateTime.now()
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
	
	@NotNull
	@NonNull
	@NotBlank
	private String mimeType;
	
	@NotNull
	@NonNull
	@NotBlank
	private ZonedDateTime uploadDateTime;
}
