package tech.ebp.oqm.core.api.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.exception.InvalidConfigException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service for managing temporary files and directories.
 * <p>
 * Creates unique temporary files and directories with timestamp-based naming to prevent conflicts.
 * Files are automatically scheduled for deletion on JVM exit via {@link File#deleteOnExit()}.
 * Supports optional subdirectory organization via the {@code tempFolder} parameter.
 * </p>
 * <p>
 * Main usages:
 * <ul>
 *     <li>Creating temporary files with custom names: {@link #getTempFile(String, String)}</li>
 *     <li>Creating uniquely named temporary files: {@link #getTempFile(String, String, String)}</li>
 *     <li>Creating temporary directories: {@link #getTempDir(String, String)}</li>
 * </ul>
 * </p>
 * <p>
 * Configuration: The base temporary directory is configured via the {@code service.tempDir} property.
 * </p>
 */
@Slf4j
@ApplicationScoped
public class TempFileService {
	
	private static final String TEMP_DIR_FORMAT = "%s_%s_%s";
	private static final String TEMP_FILE_FORMAT = TEMP_DIR_FORMAT + ".%s";
	private static final DateTimeFormatter FILENAME_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy_kk-mm");
	private static final Random RANDOM = new SecureRandom();
	
	/**
	 * Validates that a directory exists, is actually a directory, and is writable.
	 * Creates the directory if it doesn't exist.
	 *
	 * @param dir the path to validate
	 * @throws InvalidConfigException if the directory doesn't exist and cannot be created,
	 *                                is not a directory, or is not writable
	 */
	public static void checkDir(Path dir){
		if (!Files.exists(dir)) {
			try{
				dir = Files.createDirectories(dir);
			} catch(IOException e) {
				throw new InvalidConfigException("Temp directory could not be created. Dir: " + dir, e);
			}
		}
		
		if(!Files.isDirectory(dir)){
			throw new InvalidConfigException("Temp directory must be directory. Dir: " + dir);
		}
		
		if(!Files.isWritable(dir)){
			throw new InvalidConfigException("Temp directory cannot be written to. Dir: " + dir);
		}
	}
	
	private static String getRandFileInts(){
		return RANDOM.ints(3, 0, 10)
			.mapToObj(String::valueOf)
			.collect(Collectors.joining());
	}
	
	@Getter
	@ConfigProperty(name = "service.tempDir")
	Path tempDir;
	
	@PostConstruct
	public void setup() {
		log.info("Setting up temp directory: {}", this.tempDir);
		
		checkDir(this.tempDir);
		
		log.info("Done setting up temp directory.");
	}
	
	private Path getDir(String tempFolder){
		Path directory = this.tempDir;
		if (tempFolder != null && !tempFolder.isBlank()) {
			directory = directory.resolve(tempFolder);
			checkDir(directory);
		}
		return directory;
	}
	
	/**
	 * Gets a temporary file with the specified filename in the given folder.
	 * The file is scheduled for deletion on JVM exit.
	 *
	 * @param filename the filename to use
	 * @param tempFolder optional subdirectory within the temp directory (can be null/empty)
	 * @return a File object pointing to the temp file
	 */
	public File getTempFile(String filename, String tempFolder){
		Path directory = this.getDir(tempFolder);
		
		File output = new File(
			directory.toFile(),
			filename
		);
		output.deleteOnExit();
		return output;
	}
	
	
	/**
	 * Gets a uniquely named temporary file with the given prefix and extension.
	 * The filename format is: {@code prefix_MM-dd-yyyy_kk-mm_rrr.extension}
	 * where rrr is a random 3-digit number.
	 * The file is scheduled for deletion on JVM exit.
	 *
	 * @param prefix the filename prefix
	 * @param extension the file extension (without dot)
	 * @param tempFolder optional subdirectory within the temp directory (can be null/empty)
	 * @return a File object pointing to the temp file
	 */
	public File getTempFile(String prefix, String extension, String tempFolder) {
		return this.getTempFile(
			String.format(
				TEMP_FILE_FORMAT,
				prefix,
				ZonedDateTime.now().format(FILENAME_TIMESTAMP_FORMAT),
				getRandFileInts(),
				extension
			),
			tempFolder
		);
	}
	
	
	/**
	 * Creates a uniquely named temporary directory with the given prefix.
	 * The directory name format is: {@code prefix_MM-dd-yyyy_kk-mm_rrr}
	 * where rrr is a random 3-digit number.
	 * The directory is scheduled for deletion on JVM exit.
	 *
	 * @param prefix the directory name prefix
	 * @param tempFolder optional subdirectory within the temp directory (can be null/empty)
	 * @return a File object pointing to the temp directory
	 */
	public File getTempDir(String prefix, String tempFolder) {
		Path directory = this.getDir(tempFolder);
		
		directory = directory.resolve(
			String.format(
				TEMP_DIR_FORMAT,
				prefix,
				ZonedDateTime.now().format(FILENAME_TIMESTAMP_FORMAT),
				getRandFileInts()
			)
		);
		
		checkDir(directory);
		File output = directory.toFile();
		output.deleteOnExit();
		
		return output;
	}
}
