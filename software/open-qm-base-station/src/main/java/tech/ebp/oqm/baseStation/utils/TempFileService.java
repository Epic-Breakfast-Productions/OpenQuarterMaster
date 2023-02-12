package tech.ebp.oqm.baseStation.utils;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.exception.InvalidConfigException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
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
	
	@ConfigProperty(name = "service.tempDir")
	String tempLocation;
	
	File tempDir;
	
	@PostConstruct
	public void setup() {
		log.info("Setting up temp directory.");
		
		this.tempDir = new File(tempLocation);
		
		if (!this.tempDir.exists() && !this.tempDir.mkdirs()) {
			throw new InvalidConfigException("Temp directory could not be created. Dir: " + this.tempLocation);
		}
		
		//TODO:: test can write to the temp dir?
		
		log.info("Done setting up temp directory.");
	}
	
	
	public File getTempFile(String prefix, String extension, String tempFolder) {
		File directory = this.tempDir;
		if (tempFolder != null && !tempFolder.isBlank()) {
			directory = new File(directory, tempFolder);
			if (!directory.mkdirs()) {
				throw new IllegalStateException("Failed to create temp folder under main temp: " + directory);
			}
		}
		
		File output = new File(
			directory,
			String.format(
				TEMP_FILE_FORMAT,
				prefix,
				ZonedDateTime.now().format(FILENAME_TIMESTAMP_FORMAT),
				rand.ints(3, 0, 10)
					.mapToObj(String::valueOf)
					.collect(Collectors.joining()),
				extension
			)
		);
		output.deleteOnExit();
		return output;
	}
	
	public File getTempFile(String prefix, String extension) {
		return this.getTempFile(prefix, extension, null);
	}
}
