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
import tech.ebp.oqm.core.characteristics.model.characteristics.Characteristics;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@ApplicationScoped
public class CharacteristicsService {
	private static final String CHARACTERISTICS_CACHE_NAME = "characteristic-cache";
	
	@Inject
	CharacteristicsConfig characteristicsConfig;
	
	@Inject
	ObjectMapper objectMapper;
	
	
	void populateCharacteristics(Characteristics.CharacteristicsBuilder builder, ObjectNode characteristicsNode){
		//TODO:: read data
	}
	
	
	private Characteristics getCharacteristics(Path file){
		JsonNode charJson = null;
		try {
			//TODO:: if dir, apply default config file name
			charJson = this.objectMapper.readTree(file.toFile());
		} catch(IOException e) {
			throw new FailedReadingCharacteristicsException(e);
		}
		
		if( !(charJson instanceof ObjectNode) ){
			throw new FailedReadingCharacteristicsException("Characteristics from file were not resolved as an object.");
		}
		
		Characteristics.CharacteristicsBuilder builder = Characteristics.builder();
		
		populateCharacteristics(builder, (ObjectNode) charJson);
		
		return builder.build();
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
