package tech.ebp.oqm.baseStation.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.exception.InvalidConfigException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
	
	private static final String TEMP_FILE_FORMAT = "%s_%s_%s.%s";
	private static final DateTimeFormatter FILENAME_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy_kk-mm");
	private static final Random rand = new SecureRandom();
	
	private static void checkDir(Path dir){
		if (!Files.exists(dir)) {
			try{
				dir = Files.createDirectories(dir);
			} catch(IOException e) {
				throw new InvalidConfigException("Temp directory could not be created. Dir: " + dir);
			}
		}
		
		if(!Files.isDirectory(dir)){
			throw new InvalidConfigException("Temp directory must be directory. \"Dir\": " + dir);
		}
		
		if(!Files.isWritable(dir)){
			throw new InvalidConfigException("Temp directory cannot be written to. Dir: " + dir);
		}
	}
	
	@ConfigProperty(name = "service.tempDir")
	Path tempDir;
	
	@PostConstruct
	public void setup() {
		log.info("Setting up temp directory: {}", this.tempDir);
		
		checkDir(this.tempDir);
		
		log.info("Done setting up temp directory.");
	}
	
	public File getTempFile(String filename, String tempFolder){
		Path directory = this.tempDir;
		if (tempFolder != null && !tempFolder.isBlank()) {
			directory = directory.resolve(tempFolder);
			
			checkDir(directory);
			
		}
		
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
				rand.ints(3, 0, 10)
					.mapToObj(String::valueOf)
					.collect(Collectors.joining()),
				extension
			),
			tempFolder
		);
	}
}
