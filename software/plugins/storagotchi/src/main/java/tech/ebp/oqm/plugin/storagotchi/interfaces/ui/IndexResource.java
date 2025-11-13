package tech.ebp.oqm.plugin.storagotchi.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.storagotchi.model.UserSettings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Path("/")
public class IndexResource {
	
	private static final Map<String, List<String>> STUFF_MAP = new HashMap<>();
	
	static {
		URL stuffUri = IndexResource.class.getResource("/META-INF/resources/media/sprites/stuff");
		
		java.nio.file.Path stuffPath;
		try {
			stuffPath = Paths.get(stuffUri.toURI());
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
		log.info("Stuff URI: {}", stuffUri);
		
		try(Stream<java.nio.file.Path> curFileStream = Files.walk(stuffPath)){
			for(java.nio.file.Path curFile : curFileStream.toList()){
				if(Files.isDirectory(curFile)){
					continue;
				}
				log.info("Found File: {}", curFile);
				
				String rarity = curFile.getParent().toString();
				rarity = rarity.substring(rarity.lastIndexOf('/') + 1).trim();
				log.info("Rarity: {}", rarity);
				
				if(!STUFF_MAP.containsKey(rarity)){
					STUFF_MAP.put(rarity, new ArrayList<>());
				}
				STUFF_MAP.get(rarity).add(curFile.getFileName().toString());
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	@Location("index")
	Template index;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance game() {
		UserSettings settings = UserSettings.builder().build();
		
		return this.index
				   .data("stuffMap", STUFF_MAP)
				   .data("settings", settings)
			;
	}
}
