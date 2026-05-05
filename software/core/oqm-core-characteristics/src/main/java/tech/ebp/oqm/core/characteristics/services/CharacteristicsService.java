package tech.ebp.oqm.core.characteristics.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.core.characteristics.config.CharacteristicsConfig;
import tech.ebp.oqm.core.characteristics.exception.FailedReadingCharacteristicsException;
import tech.ebp.oqm.core.characteristics.model.characteristics.Banner;
import tech.ebp.oqm.core.characteristics.model.characteristics.Characteristics;
import tech.ebp.oqm.core.characteristics.model.characteristics.RunBy;
import tech.ebp.oqm.core.characteristics.utils.ColorUtils;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@ApplicationScoped
public class CharacteristicsService {
	private static final String CHARACTERISTICS_CACHE_NAME = "characteristic-cache";
	private static final String CHARACTERISTICS_DEFAULT_FILE_NAME = "characteristics.yaml";
	
	
	@Inject
	CharacteristicsConfig characteristicsConfig;
	
	@Inject
	ColorUtils colorUtils;
	
	@Inject
	ObjectMapper objectMapper;
	
	
	private String getStringVal(JsonNode node){
		return (node != null && node.isTextual()) ? node.asText() : null;
	}
	private Color getColorVal(JsonNode node){
		String colorStr = getStringVal(node);
		return this.colorUtils.getColor(colorStr);
	}
	
	
	public Characteristics getCharacteristics(ObjectNode characteristicsNode){
		Characteristics.CharacteristicsBuilder builder = Characteristics.builder();
		
		builder.title(getStringVal(characteristicsNode.get("title")));
		builder.motd(getStringVal(characteristicsNode.get("motd")));
		
		if(characteristicsNode.has("runBy")){
			ObjectNode runByNode = (ObjectNode) characteristicsNode.get("runBy");
			RunBy.RunByBuilder runByBuilder = RunBy.builder();
			
			runByBuilder.name(getStringVal(runByNode.get("name")));
			runByBuilder.email(getStringVal(runByNode.get("email")));
			runByBuilder.phone(getStringVal(runByNode.get("phone")));
			runByBuilder.website(getStringVal(runByNode.get("website")));
			
			//TODO:: validate path exists, default file
			String logoPath = getStringVal(runByNode.get("logoImg"));
			runByBuilder.logoPath(
				Paths.get(logoPath).toAbsolutePath().normalize()
			);
			String bannerPath = getStringVal(runByNode.get("bannerImg"));
			runByBuilder.bannerPath(
				Paths.get(bannerPath).toAbsolutePath().normalize()
			);
			
			builder.runBy(runByBuilder.build());
		}
		
		if(characteristicsNode.has("banner")) {
			ObjectNode runByNode = (ObjectNode) characteristicsNode.get("banner");
			Banner.BannerBuilder bannerBuilder = Banner.builder();
			
			bannerBuilder.text(getStringVal(runByNode.get("text")));
			bannerBuilder.textColor(getColorVal(runByNode.get("textColor")));
			bannerBuilder.backgroundColor(getColorVal(runByNode.get("backgroundColor")));
			
			builder.banner(bannerBuilder.build());
		}
		
		return builder.build();
	}
	
	
	public Characteristics getCharacteristics(Path file){
		if(Files.isDirectory(file)){
			file = file.resolve(CHARACTERISTICS_DEFAULT_FILE_NAME);
		}
		
		if(!Files.exists(file)){
			throw new FailedReadingCharacteristicsException("Characteristics file does not exist: " + file);
		}
		
		if(!Files.isReadable(file)){
			throw new FailedReadingCharacteristicsException("Characteristics file is not readable: " + file);
		}
		
		JsonNode charJson = null;
		try {
			charJson = this.objectMapper.readTree(file.toFile());
		} catch(IOException e) {
			throw new FailedReadingCharacteristicsException("Failed to parse characteristics file.", e);
		}
		
		if( !(charJson instanceof ObjectNode) ){
			throw new FailedReadingCharacteristicsException("Characteristics from file were not resolved as an object.");
		}
		
		return getCharacteristics((ObjectNode) charJson);
	}
	
	
	
	
	
	
	
	
	
	@CacheResult(cacheName = CHARACTERISTICS_CACHE_NAME)
	public Characteristics getCharacteristics() {
		return this.getCharacteristics(this.characteristicsConfig.fileLocation());
	}
	
	@CacheInvalidate(cacheName = CHARACTERISTICS_CACHE_NAME)
	public void newFile(){
		log.info("Invalidating cache.");
	}
	
	//TODO:: images
}
