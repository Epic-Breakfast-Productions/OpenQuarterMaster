package tech.ebp.oqm.lib.core.object.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.tika.Tika;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
	
	private static final Tika TIKA = new Tika();
	
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
