package tech.ebp.oqm.core.api.model.object.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class FileHashes {
	
	@NonNull
	@NotNull
	@NotBlank
	private String md5;
	@NonNull
	@NotNull
	@NotBlank
	private String sha1;
	@NonNull
	@NotNull
	@NotBlank
	private String sha256;
	
	
	/**
	 * If proves too slow, or if need to use input stream, use the howtoinjava method to read in all at same time
	 * <p>
	 * https://howtodoinjava.com/java/java-security/sha-md5-file-checksum-hash/
	 * <p>
	 * https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
	 */
	public static FileHashes fromFile(File file) {
		FileHashes.FileHashesBuilder<?, ?> builder = FileHashes.builder();
		
		try (FileInputStream is = new FileInputStream(file)) {
			builder.md5(DigestUtils.md5Hex(is));
		} catch(IOException e) {
			throw new RuntimeException("Failed to get md5 hash of file- " + e.getMessage(), e);
		}
		
		try (FileInputStream is = new FileInputStream(file)) {
			builder.sha1(DigestUtils.sha1Hex(is));
		} catch(IOException e) {
			throw new RuntimeException("Failed to get SHA1 hash of file- " + e.getMessage(), e);
		}
		
		try (FileInputStream is = new FileInputStream(file)) {
			builder.sha256(DigestUtils.sha256Hex(is));
		} catch(IOException e) {
			throw new RuntimeException("Failed to get SHA256 hash of file- " + e.getMessage(), e);
		}
		
		return builder.build();
	}
	
}
