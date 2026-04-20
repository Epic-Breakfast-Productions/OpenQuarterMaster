package tech.ebp.oqm.core.api.model.object.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(description = "The hashes of a file.")
public class FileHashes {
	
	@NonNull
	@NotNull
	@NotBlank
	@Schema(description = "The MD5 hash of the file.", examples = {"65578da0358a01f31a6a78dfcf47e2bf"})
	private String md5;
	
	@NonNull
	@NotNull
	@NotBlank
	@Schema(description = "The Sha1 hash of the file.", examples = {"c7d2c195e8c87163d2635ddb19792e7591ed0bfb"})
	private String sha1;
	
	@NonNull
	@NotNull
	@NotBlank
	@Schema(description = "The Sha256 hash of the file.", examples = {"aed171fb114f82e6eaea4970a245a200e0582a7dcc8ec0891ca41b6e4a62b754"})
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
