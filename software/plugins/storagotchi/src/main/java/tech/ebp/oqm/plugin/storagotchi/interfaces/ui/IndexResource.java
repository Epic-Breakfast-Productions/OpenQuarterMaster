package tech.ebp.oqm.plugin.storagotchi.interfaces.ui;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.plugin.storagotchi.model.usersettings.UserSettings;
import tech.ebp.oqm.plugin.storagotchi.model.usersettings.UserSettingsService;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Path("/")
@Authenticated
@RequestScoped
public class IndexResource {
	
	private static final String STUFF_PATH = "/META-INF/resources/media/sprites/stuff";
	private static final Map<String, List<String>> STUFF_MAP = new HashMap<>();
	
	static {
		URL stuffUrl = IndexResource.class.getResource(STUFF_PATH);
		
		log.debug("Stuff located in: {}", stuffUrl);
		FileSystem fileSystem = null;
		try{
			java.nio.file.Path stuffPath;
			try {
				URI stuffUri = stuffUrl.toURI();
				if (stuffUri.getScheme().equals("jar")) {
					fileSystem = FileSystems.newFileSystem(stuffUri, Collections.emptyMap());
					stuffPath = fileSystem.getPath(STUFF_PATH);
				} else {
					stuffPath = Paths.get(stuffUri);
				}
			} catch(URISyntaxException | IOException e) {
				throw new RuntimeException(e);
			}
			log.info("Stuff path: {}", stuffPath);
			
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
			
		} finally {
			if(fileSystem != null){//dumb workaround to ensure we nuke the filesystem (if it exists)
				try(FileSystem sys2 = fileSystem){
					log.debug("Had a filesystem that needed closing.");
				} catch(IOException e){
					throw new RuntimeException("Failed to close filesystem. This should not happen.", e);
				}
			}
		}
	}
	
	@Getter
	@HeaderParam("x-forwarded-prefix")
	Optional<String> forwardedPrefix;
	
	protected String getRootPrefix(){
		return this.forwardedPrefix.orElse("");
	}
	
	@Inject
	UserSettingsService userSettingsService;
	
	@Context
	SecurityContext securityContext;
	
	@Location("index")
	Template index;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public TemplateInstance game() {
		
		return this.index
				   .data("rootPrefix", this.getRootPrefix())
				   .data("stuffMap", STUFF_MAP)
				   .data("settings", this.userSettingsService.getByIdOrDefault(securityContext.getUserPrincipal().getName()))
			;
	}
}
