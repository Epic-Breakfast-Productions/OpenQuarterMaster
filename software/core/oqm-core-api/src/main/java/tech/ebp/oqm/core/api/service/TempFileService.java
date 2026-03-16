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

@Slf4j
@ApplicationScoped
public class TempFileService {
	
	private static final String TEMP_DIR_FORMAT = "%s_%s_%s";
	private static final String TEMP_FILE_FORMAT = TEMP_DIR_FORMAT + ".%s";
	private static final DateTimeFormatter FILENAME_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy_kk-mm");
	private static final Random RANDOM = new SecureRandom();
	
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
	
	public File getTempFile(String filename, String tempFolder){
		Path directory = this.getDir(tempFolder);
		
		File output = new File(
			directory.toFile(),
			filename
		);
		output.deleteOnExit();
		return output;
	}
	
	
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
